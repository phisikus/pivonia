package eu.phisikus.pivonia.integration;

import eu.phisikus.pivonia.api.pool.Envelope;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
class TimeMessage implements Envelope<String> {
    private String senderId;
    private String recipientId;
    private Long timestamp;

    @Override
    public Envelope<String> readdress(String senderId, String recipientId) {
        return new TimeMessage(senderId, recipientId, this.timestamp);
    }
}
