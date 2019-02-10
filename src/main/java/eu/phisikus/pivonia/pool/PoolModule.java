package eu.phisikus.pivonia.pool;

import dagger.Module;
import dagger.Provides;
import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.pool.address.AddressPoolModule;
import eu.phisikus.pivonia.pool.client.ClientPoolModule;
import eu.phisikus.pivonia.pool.heartbeat.HeartbeatPoolModule;
import eu.phisikus.pivonia.pool.mediators.ConnectionManagerImpl;
import eu.phisikus.pivonia.qualifiers.Encrypted;
import eu.phisikus.pivonia.qualifiers.PlainText;

import javax.inject.Provider;

@Module(includes = {
        HeartbeatPoolModule.class,
        ClientPoolModule.class,
        AddressPoolModule.class
})
public class PoolModule {

    private final int maxConnectionRetryAttempts;

    public PoolModule(int maxConnectionRetryAttempts) {
        this.maxConnectionRetryAttempts = maxConnectionRetryAttempts;
    }

    @Provides
    public ConnectionManager provideConnectionManager(ClientPool clientPool,
                                                      AddressPool addressPool,
                                                      HeartbeatPool heartbeatPool,
                                                      Provider<Client> clientProvider) {
        return new ConnectionManagerImpl(clientPool, addressPool, heartbeatPool, clientProvider, maxConnectionRetryAttempts);
    }
}
