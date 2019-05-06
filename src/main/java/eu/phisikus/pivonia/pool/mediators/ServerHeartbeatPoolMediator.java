package eu.phisikus.pivonia.pool.mediators;

import eu.phisikus.pivonia.api.Transmitter;
import eu.phisikus.pivonia.pool.ServerHeartbeatPool;
import eu.phisikus.pivonia.pool.ServerPool;
import eu.phisikus.pivonia.pool.TransmitterPool;
import eu.phisikus.pivonia.pool.heartbeat.events.HeartbeatPoolEvent;
import eu.phisikus.pivonia.pool.heartbeat.events.ReceivedEvent;
import eu.phisikus.pivonia.pool.heartbeat.events.TimeoutEvent;
import eu.phisikus.pivonia.pool.server.ServerPoolEvent;
import io.reactivex.disposables.Disposable;
import lombok.extern.log4j.Log4j2;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Connects ServerPool with ServerHeartbeatPool.
 * Each server added to the server pool will be also added to the server heartbeat pool.
 * It means that listener for heartbeat messages will be registered for those servers.
 * Each received heartbeat response will assign server-connected Transmitter to specific nodeId in the TransmitterPool.
 * Each heartbeat timeout will cause that transmitter to be removed from the TransmitterPool.
 */
@Log4j2
public class ServerHeartbeatPoolMediator<K> implements Disposable {
    private Disposable additionSubscription;
    private Disposable removalSubscription;
    private Disposable assignmentSubscription;
    private Disposable timeoutSubscription;
    private Set<Transmitter> addedTransmitters = ConcurrentHashMap.newKeySet();

    public ServerHeartbeatPoolMediator(
            ServerPool serverPool,
            TransmitterPool<K> transmitterPool,
            ServerHeartbeatPool<K> serverHeartbeatPool) {
        bind(serverPool, transmitterPool, serverHeartbeatPool);
    }

    private void bind(ServerPool serverPool,
                      TransmitterPool<K> transmitterPool,
                      ServerHeartbeatPool<K> serverHeartbeatPool) {
        var serverChanges = serverPool.getChanges();
        var heartbeatChanges = serverHeartbeatPool.getHeartbeatChanges();

        additionSubscription = serverChanges
                .filter(serverPoolEvent -> serverPoolEvent.getOperation() == ServerPoolEvent.Operation.ADD)
                .subscribe(serverPoolEvent -> serverHeartbeatPool.add(serverPoolEvent.getServer()));

        removalSubscription = serverChanges
                .filter(serverPoolEvent -> serverPoolEvent.getOperation() == ServerPoolEvent.Operation.REMOVE)
                .subscribe(transmitterPoolEvent -> serverHeartbeatPool.remove(transmitterPoolEvent.getServer()));

        assignmentSubscription = heartbeatChanges
                .filter(heartbeatPoolEvent -> heartbeatPoolEvent.getOperation() == HeartbeatPoolEvent.Operation.RECEIVED)
                .map(heartbeatPoolEvent -> (ReceivedEvent<K>) heartbeatPoolEvent)
                .subscribe(event -> handleHeartbeatReceivedEvent(event, transmitterPool));

        timeoutSubscription = heartbeatChanges
                .filter(heartbeatPoolEvent -> heartbeatPoolEvent.getOperation() == HeartbeatPoolEvent.Operation.TIMEOUT)
                .map(heartbeatPoolEvent -> (TimeoutEvent) heartbeatPoolEvent)
                .subscribe(event -> handleTimeoutEvent(event, transmitterPool));
    }

    private void handleTimeoutEvent(TimeoutEvent event, TransmitterPool<K> transmitterPool) {
        var transmitter = event.getTransmitter();
        addedTransmitters.remove(transmitter);
        transmitterPool.remove(transmitter);
    }

    private void handleHeartbeatReceivedEvent(ReceivedEvent<K> event, TransmitterPool<K> transmitterPool) {
        var transmitter = event.getTransmitter();
        if (!addedTransmitters.contains(transmitter)) {
            transmitterPool.add(transmitter);
        }
        transmitterPool.set(event.getId(), transmitter);
    }

    @Override
    public void dispose() {
        additionSubscription.dispose();
        removalSubscription.dispose();
        assignmentSubscription.dispose();
        timeoutSubscription.dispose();
    }

    @Override
    public boolean isDisposed() {
        return additionSubscription.isDisposed() &&
                removalSubscription.isDisposed() &&
                assignmentSubscription.isDisposed() &&
                timeoutSubscription.isDisposed();
    }

}
