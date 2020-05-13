package eu.phisikus.pivonia.pool.heartbeat;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Value;

@Value
class HeartbeatMessage<K> {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@className")
    private K senderId;
    private Long timestamp;
}
