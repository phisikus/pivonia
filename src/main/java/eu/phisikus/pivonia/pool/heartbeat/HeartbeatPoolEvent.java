package eu.phisikus.pivonia.pool.heartbeat;

import eu.phisikus.pivonia.api.Client;
import lombok.Value;

@Value
public class HeartbeatPoolEvent<K> {
    private final K id;
    private final Client client;
    private final Operation operation;

    public enum Operation {
        RECEIVED, TIMEOUT
    }
}
