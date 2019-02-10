package eu.phisikus.pivonia.pool.mediators;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.pool.AddressPool;
import eu.phisikus.pivonia.pool.ClientPool;
import eu.phisikus.pivonia.pool.ConnectionManager;
import eu.phisikus.pivonia.pool.HeartbeatPool;
import lombok.AccessLevel;
import lombok.Getter;

import javax.inject.Provider;

@Getter
class ConnectionManagerImpl<K> implements ConnectionManager<K> {
    private final ClientPool<K> clientPool;
    private final AddressPool addressPool;
    private final HeartbeatPool<K> heartbeatPool;

    @Getter(AccessLevel.NONE)
    private final AddressClientPoolMediator addressClientPoolMediator;
    @Getter(AccessLevel.NONE)
    private final ClientHeartbeatPoolMediator<K> clientHeartbeatPoolMediator;

    public ConnectionManagerImpl(ClientPool<K> clientPool,
                                 AddressPool addressPool,
                                 HeartbeatPool<K> heartbeatPool,
                                 Provider<Client> clientProvider,
                                 int maxRetryAttempts) {
        this.clientPool = clientPool;
        this.addressPool = addressPool;
        this.heartbeatPool = heartbeatPool;
        this.addressClientPoolMediator = new AddressClientPoolMediator(clientPool, addressPool, clientProvider, maxRetryAttempts);
        this.clientHeartbeatPoolMediator = new ClientHeartbeatPoolMediator<>(clientPool, heartbeatPool);
    }

    @Override
    public void dispose() {
        addressClientPoolMediator.dispose();
        clientHeartbeatPoolMediator.dispose();
    }

    @Override
    public boolean isDisposed() {
        return addressClientPoolMediator.isDisposed() &&
                clientHeartbeatPoolMediator.isDisposed();
    }
}
