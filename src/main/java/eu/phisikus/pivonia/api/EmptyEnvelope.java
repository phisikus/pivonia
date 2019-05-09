package eu.phisikus.pivonia.api;

import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class EmptyEnvelope<K> implements Envelope<K> {
    protected K senderId;
    protected K recipientId;

    @Override
    public Envelope<K> readdress(K senderId, K recipientId) {
        return new EmptyEnvelope<>(senderId, recipientId);
    }
}
