package eu.phisikus.pivonia.api;

import lombok.Value;

@Value
public class EmptyEnvelope<K> implements Envelope<K> {
    protected K senderId;
    protected K recipientId;

    @Override
    public Envelope<K> readdress(K senderId, K recipientId) {
        return new EmptyEnvelope<>(senderId, recipientId);
    }
}
