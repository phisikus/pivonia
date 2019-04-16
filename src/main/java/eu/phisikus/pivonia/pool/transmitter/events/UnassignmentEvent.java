package eu.phisikus.pivonia.pool.transmitter.events;

import eu.phisikus.pivonia.api.Transmitter;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Represents unassignment of ID to transmitter instance.
 *
 * @param <K> type of ID
 */
@EqualsAndHashCode(callSuper = true)
public class UnassignmentEvent<K> extends TransmitterPoolEvent {

    @Getter
    private final K id;

    public UnassignmentEvent(K id, Transmitter transmitter) {
        super(transmitter, Operation.UNASSIGN);
        this.id = id;
    }
}
