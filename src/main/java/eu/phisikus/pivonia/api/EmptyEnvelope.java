package eu.phisikus.pivonia.api;

import eu.phisikus.pivonia.api.pool.Envelope;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmptyEnvelope<K> implements Envelope<K> {
    private K senderId;
    private K recipientId;

    @Override
    public Envelope<K> readdress(K senderId, K recipientId) {
        return new EmptyEnvelope<>(senderId, recipientId);
    }
}
