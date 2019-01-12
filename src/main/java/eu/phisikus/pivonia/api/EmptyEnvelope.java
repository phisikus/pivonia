package eu.phisikus.pivonia.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
