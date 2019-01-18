package eu.phisikus.pivonia.pool.heartbeat;

import eu.phisikus.pivonia.api.EmptyEnvelope;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class HeartbeatMessage<K> extends EmptyEnvelope<K> {
    public HeartbeatMessage(K senderId, K recipientId) {
        super(senderId, recipientId);
    }
}
