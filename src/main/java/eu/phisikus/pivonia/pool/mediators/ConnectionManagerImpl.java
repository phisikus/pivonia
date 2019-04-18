package eu.phisikus.pivonia.pool.mediators;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.pool.AddressPool;
import eu.phisikus.pivonia.pool.ConnectionManager;
import eu.phisikus.pivonia.pool.HeartbeatPool;
import eu.phisikus.pivonia.pool.ServerPool;
import eu.phisikus.pivonia.pool.TransmitterPool;
import lombok.AccessLevel;
import lombok.Getter;

import javax.inject.Provider;

@Getter
public class ConnectionManagerImpl<K> implements ConnectionManager<K> {
    private final TransmitterPool<K> transmitterPool;
    private final AddressPool addressPool;
    private final HeartbeatPool<K> heartbeatPool;
    private final ServerPool serverPool;

    @Getter(AccessLevel.NONE)
    private final AddressTransmitterPoolMediator addressTransmitterPoolMediator;
    @Getter(AccessLevel.NONE)
    private final ClientHeartbeatPoolMediator<K> clientHeartbeatPoolMediator;

    public ConnectionManagerImpl(TransmitterPool<K> transmitterPool,
                                 AddressPool addressPool,
                                 HeartbeatPool<K> heartbeatPool,
                                 ServerPool serverPool,
                                 Provider<Client> clientProvider,
                                 int maxRetryAttempts) {
        this.transmitterPool = transmitterPool;
        this.addressPool = addressPool;
        this.heartbeatPool = heartbeatPool;
        this.serverPool = serverPool;
        this.addressTransmitterPoolMediator = new AddressTransmitterPoolMediator(transmitterPool, addressPool, clientProvider, maxRetryAttempts);
        this.clientHeartbeatPoolMediator = new ClientHeartbeatPoolMediator<>(transmitterPool, heartbeatPool);
    }

    @Override
    public void dispose() {
        addressTransmitterPoolMediator.dispose();
        clientHeartbeatPoolMediator.dispose();
    }

    @Override
    public boolean isDisposed() {
        return addressTransmitterPoolMediator.isDisposed() &&
                clientHeartbeatPoolMediator.isDisposed();
    }
}
