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

    private final int retryAttempts;

    public PoolModule(int maxConnectionRetryAttempts) {
        this.retryAttempts = maxConnectionRetryAttempts;
    }

    @Provides
    public <K> ConnectionManager<K> provideConnectionManager(
            HeartbeatPool<K> heartbeatPool,
            ClientPool<K> clientPool,
            AddressPool addressPool,
            Provider<Client> clientProvider) {
        return new ConnectionManagerImpl<>(clientPool, addressPool, heartbeatPool, clientProvider, retryAttempts);
    }
}
