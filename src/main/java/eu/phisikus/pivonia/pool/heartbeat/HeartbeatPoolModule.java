package eu.phisikus.pivonia.pool.heartbeat;

import dagger.Module;
import dagger.Provides;
import eu.phisikus.pivonia.pool.ClientHeartbeatPool;
import eu.phisikus.pivonia.pool.ServerHeartbeatPool;

@Module
public class HeartbeatPoolModule {

    private final long heartbeatDelay;
    private final long timeoutDelay;
    private final Object nodeId;

    public HeartbeatPoolModule(long heartbeatDelay, long timeoutDelay, Object nodeId) {
        this.heartbeatDelay = heartbeatDelay;
        this.timeoutDelay = timeoutDelay;
        this.nodeId = nodeId;
    }

    @Provides
    public ClientHeartbeatPool provideClientHeartbeatPool() {
        return new ClientHeartbeatPoolImpl(heartbeatDelay, timeoutDelay, nodeId);
    }

    @Provides
    public ServerHeartbeatPool provideServerHeartbeatPool() {
        return new ServerHeartbeatPoolImpl(heartbeatDelay, timeoutDelay, nodeId);
    }
}
