package eu.phisikus.pivonia.pool.transmitter;

import dagger.Module;
import dagger.Provides;
import eu.phisikus.pivonia.pool.TransmitterPool;

@Module
public class ClientPoolModule {

    @Provides
    public TransmitterPool provideClientPool() {
        return new TransmitterPoolImpl();
    }
}
