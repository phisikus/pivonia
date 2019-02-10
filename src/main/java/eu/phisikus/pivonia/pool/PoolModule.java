package eu.phisikus.pivonia.pool;

import dagger.Module;
import dagger.Provides;
import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.pool.address.AddressPoolModule;
import eu.phisikus.pivonia.pool.client.ClientPoolModule;
import eu.phisikus.pivonia.pool.heartbeat.HeartbeatPoolModule;
import eu.phisikus.pivonia.pool.mediators.ConnectionManagerImpl;

import javax.inject.Provider;

@Module(includes = {
        HeartbeatPoolModule.class,
        ClientPoolModule.class,
        AddressPoolModule.class
})
public class PoolModule {

    private final Provider<Client> clientProvider;
    private final int maxConnectionRetryAttempts;

    public PoolModule(Provider<Client> clientProvider, int maxConnectionRetryAttempts) {
        this.clientProvider = clientProvider;
        this.maxConnectionRetryAttempts = maxConnectionRetryAttempts;
    }

    @Provides
    public ConnectionManager provideConnectionManager(ClientPool clientPool,
                                                      AddressPool addressPool,
                                                      HeartbeatPool heartbeatPool) {
        return new ConnectionManagerImpl(clientPool, addressPool, heartbeatPool, clientProvider, maxConnectionRetryAttempts);
    }
}
