package eu.phisikus.pivonia.pool.mediators;

import dagger.Module;
import dagger.Provides;
import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.pool.AddressPool;
import eu.phisikus.pivonia.pool.ClientPool;
import eu.phisikus.pivonia.pool.ConnectionManager;
import eu.phisikus.pivonia.pool.HeartbeatPool;

import javax.inject.Provider;

@Module
public class ConnectionManagerModule {

    private final int retryAttempts;

    public ConnectionManagerModule(int maxConnectionRetryAttempts) {
        this.retryAttempts = maxConnectionRetryAttempts;
    }

    @Provides
    public ConnectionManager provideConnectionManager(
            HeartbeatPool heartbeatPool,
            ClientPool clientPool,
            AddressPool addressPool,
            Provider<Client> clientProvider) {
        return new ConnectionManagerImpl<>(clientPool, addressPool, heartbeatPool, clientProvider, retryAttempts);
    }
}