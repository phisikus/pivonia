package eu.phisikus.pivonia.pool.heartbeat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class HeartbeatMessage<K> {
    private K senderId;
    private Long timestamp;
}
