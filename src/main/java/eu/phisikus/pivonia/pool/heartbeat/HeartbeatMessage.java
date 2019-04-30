package eu.phisikus.pivonia.pool.heartbeat;

import lombok.Value;

@Value
class HeartbeatMessage<K> {
    private K senderId;
    private Long timestamp;
}
