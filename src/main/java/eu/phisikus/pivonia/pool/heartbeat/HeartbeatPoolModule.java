package eu.phisikus.pivonia.pool.heartbeat;

import dagger.Module;
import dagger.Provides;
import eu.phisikus.pivonia.pool.HeartbeatPool;

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
    public HeartbeatPool provideHeartbeatPool() {
        return new HeartbeatPoolImpl(heartbeatDelay, timeoutDelay, nodeId);
    }
}
