package eu.phisikus.pivonia.pool;

import dagger.Module;
import eu.phisikus.pivonia.pool.address.AddressPoolModule;
import eu.phisikus.pivonia.pool.client.ClientPoolModule;
import eu.phisikus.pivonia.pool.heartbeat.HeartbeatPoolModule;

@Module(includes = {
        HeartbeatPoolModule.class,
        ClientPoolModule.class,
        AddressPoolModule.class
})
public class PoolModule {
}
