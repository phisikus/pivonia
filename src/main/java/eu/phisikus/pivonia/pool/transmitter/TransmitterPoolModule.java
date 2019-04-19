package eu.phisikus.pivonia.pool.transmitter;

import dagger.Module;
import dagger.Provides;
import eu.phisikus.pivonia.pool.TransmitterPool;

@Module
public class TransmitterPoolModule {

    @Provides
    public TransmitterPool provideTransmitterPool() {
        return new TransmitterPoolImpl();
    }
}
