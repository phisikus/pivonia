package eu.phisikus.pivonia.pool.mediators;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.pool.ClientPool;
import eu.phisikus.pivonia.pool.HeartbeatPool;
import eu.phisikus.pivonia.pool.client.ClientEvent;
import eu.phisikus.pivonia.pool.heartbeat.HeartbeatEvent;
import io.reactivex.disposables.Disposable;
import lombok.extern.log4j.Log4j2;

/**
 * Connects ClientPool with HeartbeatPool.
 * Each newly added client will be added to the HeartbeatPool and each removed client will be removed.
 * Each received heartbeat response will reassign client to specific nodeId in the Client Pool.
 * Each heartbeat timeout will cause the client to be closed and removed from the Client Pool
 */
@Log4j2
class ClientHeartbeatPoolMediator<K> implements Disposable {

    private Disposable additionSubscription;
    private Disposable removalSubscription;
    private Disposable assignmentSubscription;
    private Disposable timeoutSubscription;

    public ClientHeartbeatPoolMediator(ClientPool<K> clientPool, HeartbeatPool<K> heartbeatPool) {
        bind(clientPool, heartbeatPool);
    }

    private void bind(ClientPool<K> clientPool, HeartbeatPool<K> heartbeatPool) {
        var clientChanges = clientPool.getClientChanges();
        var heartbeatChanges = heartbeatPool.getHeartbeatChanges();

        additionSubscription = clientChanges
                .filter(clientEvent -> clientEvent.getOperation() == ClientEvent.Operation.ADD)
                .subscribe(clientEvent -> heartbeatPool.add(clientEvent.getClient()));

        removalSubscription = clientChanges
                .filter(clientEvent -> clientEvent.getOperation() == ClientEvent.Operation.REMOVE)
                .subscribe(clientEvent -> heartbeatPool.remove(clientEvent.getClient()));

        assignmentSubscription = heartbeatChanges
                .filter(heartbeatEvent -> heartbeatEvent.getOperation() == HeartbeatEvent.Operation.RECEIVED)
                .subscribe(heartbeatEvent -> clientPool.set(heartbeatEvent.getId(), heartbeatEvent.getClient()));

        timeoutSubscription = heartbeatChanges
                .filter(heartbeatEvent -> heartbeatEvent.getOperation() == HeartbeatEvent.Operation.TIMEOUT)
                .subscribe(heartbeatEvent -> {
                    var client = heartbeatEvent.getClient();
                    clientPool.remove(client);
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
