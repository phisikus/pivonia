package eu.phisikus.pivonia.pool.transmitter.events;

import eu.phisikus.pivonia.api.Transmitter;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Represents new assignment of ID to transmitter instance in the pool.
 *
 * @param <K> type of ID
 */
@EqualsAndHashCode(callSuper = true)
public class AssignmentEvent<K> extends TransmitterPoolEvent {

    @Getter
    private final K id;

    public AssignmentEvent(K id, Transmitter transmitter) {
        super(transmitter, Operation.ASSIGN);
        this.id = id;
    }
}
