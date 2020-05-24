package eu.phisikus.pivonia.it;

import eu.phisikus.pivonia.api.EmptyEnvelope;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;


@ToString
@EqualsAndHashCode(callSuper = true)
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