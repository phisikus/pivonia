package eu.phisikus.pivonia.pool.address;

import dagger.Module;
import dagger.Provides;
import eu.phisikus.pivonia.pool.AddressPool;

@Module
public class AddressPoolModule {

    @Provides
    public AddressPool provideAddressPool() {
        return new AddressPoolImpl();
    }
}
