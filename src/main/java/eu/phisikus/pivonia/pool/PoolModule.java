package eu.phisikus.pivonia.pool;

import dagger.Module;
import eu.phisikus.pivonia.pool.address.AddressPoolModule;
import eu.phisikus.pivonia.pool.client.ClientPoolModule;
import eu.phisikus.pivonia.pool.heartbeat.HeartbeatPoolModule;
import eu.phisikus.pivonia.pool.mediators.ConnectionManagerModule;

@Module(includes = {
        HeartbeatPoolModule.class,
        ClientPoolModule.class,
        AddressPoolModule.class,
        ConnectionManagerModule.class
})
public class PoolModule {

}
