package eu.phisikus.pivonia.pool.client;

import dagger.Module;
import dagger.Provides;
import eu.phisikus.pivonia.pool.ClientPool;

@Module
public class ClientPoolModule {

    @Provides
    public ClientPool provideClientPool() {
        return new ClientPoolImpl();
    }
}
