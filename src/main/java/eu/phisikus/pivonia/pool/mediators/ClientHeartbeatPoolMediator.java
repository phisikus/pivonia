package eu.phisikus.pivonia.pool.mediators;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.pool.ClientHeartbeatPool;
import eu.phisikus.pivonia.pool.TransmitterPool;
import eu.phisikus.pivonia.pool.heartbeat.events.HeartbeatPoolEvent;
import eu.phisikus.pivonia.pool.heartbeat.events.ReceivedEvent;
import eu.phisikus.pivonia.pool.transmitter.events.TransmitterPoolEvent;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;
import lombok.extern.log4j.Log4j2;


/**
 * Connects TransmitterPool with ClientHeartbeatPool.
 * Each newly added client will be added to the ClientHeartbeatPool and each removed client will be removed.
 * Each received heartbeat response will reassign client to specific nodeId in the TransmitterPool.
 * Each heartbeat timeout will cause the client to be closed and removed from the TransmitterPool
 */
@Log4j2
class ClientHeartbeatPoolMediator<K> implements Disposable {

    private Disposable additionSubscription;
    private Disposable removalSubscription;
    private Disposable assignmentSubscription;
    private Disposable timeoutSubscription;

    public ClientHeartbeatPoolMediator(TransmitterPool<K> transmitterPool, ClientHeartbeatPool<K> clientHeartbeatPool) {
        bind(transmitterPool, clientHeartbeatPool);
    }

    private void bind(TransmitterPool<K> transmitterPool, ClientHeartbeatPool<K> clientHeartbeatPool) {
        var transmitterChanges = transmitterPool.getChanges();
        var heartbeatChanges = clientHeartbeatPool.getHeartbeatChanges();
        Predicate<TransmitterPoolEvent> transmitterIsClient = event -> event.getTransmitter() instanceof Client;

        additionSubscription = transmitterChanges
                .filter(transmitterPoolEvent -> transmitterPoolEvent.getOperation() == TransmitterPoolEvent.Operation.ADD)
                .filter(transmitterIsClient)
                .subscribe(transmitterPoolEvent -> clientHeartbeatPool.add((Client) transmitterPoolEvent.getTransmitter()));

        removalSubscription = transmitterChanges
                .filter(transmitterPoolEvent -> transmitterPoolEvent.getOperation() == TransmitterPoolEvent.Operation.REMOVE)
                .filter(transmitterIsClient)
                .subscribe(transmitterPoolEvent -> clientHeartbeatPool.remove((Client) transmitterPoolEvent.getTransmitter()));

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
