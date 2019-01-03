package eu.phisikus.pivonia.middleware.layer.pool.test;

import eu.phisikus.pivonia.api.pool.Envelope;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FakeMessage implements Envelope<UUID> {
    private UUID senderId;
    private UUID recipientId;

    @Override
    public Envelope<UUID> readdress(UUID senderId, UUID recipientId) {
        return new FakeMessage(senderId, recipientId);
    }
}
