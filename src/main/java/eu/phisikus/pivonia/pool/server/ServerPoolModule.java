package eu.phisikus.pivonia.pool.server;

import dagger.Module;
import dagger.Provides;
import eu.phisikus.pivonia.pool.ServerPool;

@Module
public class ServerPoolModule {

    @Provides
    public ServerPool provideServerPool() {
        return new ServerPoolImpl();
    }
}
