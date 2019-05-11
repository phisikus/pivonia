package eu.phisikus.pivonia.it;

import eu.phisikus.pivonia.api.EmptyEnvelope;
import lombok.ToString;
import lombok.Value;


@ToString
@Value
public class TokenMessage extends EmptyEnvelope<Integer> {
    private Integer clock;

    public TokenMessage() {
        super(null, null);
        this.clock = 0;
    }

    public TokenMessage(Integer senderId, Integer recipientId, Integer clock) {
        super(senderId, recipientId);
        this.clock = clock;
    }
}