package eu.phisikus.pivonia.pool.client;

import dagger.Module;
import eu.phisikus.pivonia.pool.ClientPool;

@Module
public class ClientPoolModule {

    public ClientPool provideClientPool() {
        return new ClientPoolImpl();
    }
}
