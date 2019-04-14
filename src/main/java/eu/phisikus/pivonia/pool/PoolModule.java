package eu.phisikus.pivonia.pool;

import dagger.Module;
import dagger.Provides;
import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.pool.address.AddressPoolModule;
import eu.phisikus.pivonia.pool.heartbeat.HeartbeatPoolModule;
import eu.phisikus.pivonia.pool.mediators.ConnectionManagerImpl;
import eu.phisikus.pivonia.pool.server.ServerPoolModule;
import eu.phisikus.pivonia.pool.transmitter.ClientPoolModule;

import javax.inject.Provider;

@Module(includes = {
        HeartbeatPoolModule.class,
        ClientPoolModule.class,
        AddressPoolModule.class,
        ServerPoolModule.class
})
public class PoolModule {

    private final Provider<Client> clientProvider;
    private final int maxConnectionRetryAttempts;

    public PoolModule(Provider<Client> clientProvider, int maxConnectionRetryAttempts) {
        this.clientProvider = clientProvider;
        this.maxConnectionRetryAttempts = maxConnectionRetryAttempts;
    }

    @Provides
    public ConnectionManager provideConnectionManager(TransmitterPool transmitterPool,
                                                      AddressPool addressPool,
                                                      HeartbeatPool heartbeatPool) {
        return new ConnectionManagerImpl(transmitterPool, addressPool, heartbeatPool, clientProvider, maxConnectionRetryAttempts);
    }
}
