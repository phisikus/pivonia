package eu.phisikus.pivonia;

import eu.phisikus.pivonia.api.pool.Envelope;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
class TimeMessage implements Envelope<String> {
    private String senderId;
    private String recipientId;
    private Long timestamp;

    @Override
    public Envelope<String> readdress(String senderId, String recipientId) {
        return new TimeMessage(senderId, recipientId, this.timestamp);
    }
}
