package eu.phisikus.pivonia.it.mutualexclusion;

import com.fasterxml.jackson.annotation.JsonCreator;
import eu.phisikus.pivonia.api.EmptyEnvelope;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;


@ToString
@EqualsAndHashCode(callSuper = true)
@Value
public class Accept extends EmptyEnvelope<Integer> {
    private Integer clock;

    @JsonCreator
    public Accept(Integer senderId, Integer recipientId, Integer clock) {
        super(senderId, recipientId);
        this.clock = clock;
    }
}