package eu.phisikus.pivonia.pool.mediators;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.pool.HeartbeatPool;
import eu.phisikus.pivonia.pool.TransmitterPool;
import eu.phisikus.pivonia.pool.heartbeat.events.HeartbeatPoolEvent;
import eu.phisikus.pivonia.pool.heartbeat.events.ReceivedEvent;
import eu.phisikus.pivonia.pool.transmitter.events.TransmitterPoolEvent;
import io.reactivex.disposables.Disposable;
import lombok.extern.log4j.Log4j2;

/**
 * Connects TransmitterPool with HeartbeatPool.
 * Each newly added client will be added to the HeartbeatPool and each removed client will be removed.
 * Each received heartbeat response will reassign client to specific nodeId in the Transmitter Pool.
 * Each heartbeat timeout will cause the client to be closed and removed from the Transmitter Pool
 */
@Log4j2
class ClientHeartbeatPoolMediator<K> implements Disposable {

    private Disposable additionSubscription;
    private Disposable removalSubscription;
    private Disposable assignmentSubscription;
    private Disposable timeoutSubscription;

    public ClientHeartbeatPoolMediator(TransmitterPool<K> transmitterPool, HeartbeatPool<K> heartbeatPool) {
        bind(transmitterPool, heartbeatPool);
    }

    private void bind(TransmitterPool<K> transmitterPool, HeartbeatPool<K> heartbeatPool) {
        var transmitterChanges = transmitterPool.getChanges();
        var heartbeatChanges = heartbeatPool.getHeartbeatChanges();

        // TODO Instead of casting, add type detection
        additionSubscription = transmitterChanges
                .filter(transmitterPoolEvent -> transmitterPoolEvent.getOperation() == TransmitterPoolEvent.Operation.ADD)
                .subscribe(transmitterPoolEvent -> heartbeatPool.add((Client) transmitterPoolEvent.getTransmitter()));

        // TODO Instead of casting add type detection
        removalSubscription = transmitterChanges
                .filter(transmitterPoolEvent -> transmitterPoolEvent.getOperation() == TransmitterPoolEvent.Operation.REMOVE)
                .subscribe(transmitterPoolEvent -> heartbeatPool.remove((Client) transmitterPoolEvent.getTransmitter()));

        assignmentSubscription = heartbeatChanges
                .filter(heartbeatPoolEvent -> heartbeatPoolEvent.getOperation() == HeartbeatPoolEvent.Operation.RECEIVED)
                .map(heartbeatPoolEvent -> (ReceivedEvent<K>) heartbeatPoolEvent)
                .subscribe(heartbeatPoolEvent -> transmitterPool.set(heartbeatPoolEvent.getId(), heartbeatPoolEvent.getClient()));

        timeoutSubscription = heartbeatChanges
                .filter(heartbeatPoolEvent -> heartbeatPoolEvent.getOperation() == HeartbeatPoolEvent.Operation.TIMEOUT)
                .subscribe(heartbeatPoolEvent -> {
                    var client = heartbeatPoolEvent.getClient();
                    transmitterPool.remove(client);
                    closeClient(client);
                });
    }


    private void closeClient(Client client) {
        try {
            client.close();
        } catch (Exception e) {
            log.error("Unexpected exception occurred when closing client that timed out on heartbeat", e);
        }
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
