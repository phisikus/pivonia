package eu.phisikus.pivonia.it.mutualexclusion;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.phisikus.pivonia.api.EmptyEnvelope;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Value
public class Accept extends EmptyEnvelope<Integer> {
    private Integer clock;

    @JsonCreator
    public Accept(@JsonProperty("senderId") Integer senderId,
                  @JsonProperty("recipientId") Integer recipientId,
                  @JsonProperty("clock") Integer clock) {
        super(senderId, recipientId);
        this.clock = clock;
    }
}