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
public class Request extends EmptyEnvelope<Integer> {
    private Integer clock;

    @JsonCreator
    public Request(@JsonProperty("senderId") Integer senderId,
                   @JsonProperty("recipientId") Integer recipientId,
                   @JsonProperty("clock") Integer clock) {
        super(senderId, recipientId);
        this.clock = clock;
    }

    public boolean isBetterThan(Request other) {
        if (other == null) {
            return true;
        }
        final var otherId = other.senderId;
        final var thisId = this.senderId;
        return this.clock < other.clock || (clock.equals(other.clock) && thisId.compareTo(otherId) < 0);
    }
}