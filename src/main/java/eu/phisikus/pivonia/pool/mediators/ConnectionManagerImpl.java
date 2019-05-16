package eu.phisikus.pivonia.pool.mediators;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.pool.*;
import lombok.AccessLevel;
import lombok.Getter;

import javax.inject.Provider;

@Getter
public class ConnectionManagerImpl<K> implements ConnectionManager<K> {
    private final TransmitterPool<K> transmitterPool;
    private final AddressPool addressPool;
    private final ClientHeartbeatPool<K> clientHeartbeatPool;
    private final ServerHeartbeatPool<K> serverHeartbeatPool;
    private final ServerPool serverPool;

    @Getter(AccessLevel.NONE)
    private final AddressTransmitterPoolMediator addressTransmitterPoolMediator;
    @Getter(AccessLevel.NONE)
    private final ClientHeartbeatPoolMediator<K> clientHeartbeatPoolMediator;
    @Getter(AccessLevel.NONE)
    private final ServerHeartbeatPoolMediator<K> serverHeartbeatPoolMediator;

    public ConnectionManagerImpl(TransmitterPool<K> transmitterPool,
                                 AddressPool addressPool,
                                 ClientHeartbeatPool<K> clientHeartbeatPool,
                                 ServerHeartbeatPool<K> serverHeartbeatPool,
                                 ServerPool serverPool,
                                 Provider<Client> clientProvider,
                                 int maxRetryAttempts) {
        this.transmitterPool = transmitterPool;
        this.addressPool = addressPool;
        this.clientHeartbeatPool = clientHeartbeatPool;
        this.serverHeartbeatPool = serverHeartbeatPool;
        this.serverPool = serverPool;
        this.addressTransmitterPoolMediator = new AddressTransmitterPoolMediator(transmitterPool, addressPool, clientProvider, maxRetryAttempts);
        this.clientHeartbeatPoolMediator = new ClientHeartbeatPoolMediator<>(transmitterPool, clientHeartbeatPool);
        this.serverHeartbeatPoolMediator = new ServerHeartbeatPoolMediator<>(serverPool, transmitterPool, serverHeartbeatPool);
    }

    @Override
    public void dispose() {
        addressTransmitterPoolMediator.dispose();
        clientHeartbeatPoolMediator.dispose();
        serverHeartbeatPoolMediator.dispose();
        clientHeartbeatPool.dispose();
        serverHeartbeatPool.dispose();
        serverPool.dispose();
        transmitterPool.dispose();
    }

    @Override
    public boolean isDisposed() {
        return addressTransmitterPoolMediator.isDisposed() &&
                clientHeartbeatPoolMediator.isDisposed() &&
                serverHeartbeatPoolMediator.isDisposed() &&
                clientHeartbeatPool.isDisposed() &&
                serverHeartbeatPool.isDisposed() &&
                serverPool.isDisposed() &&
                transmitterPool.isDisposed();
    }
}
