package eu.phisikus.pivonia.pool.transmitter.events;

import eu.phisikus.pivonia.api.Transmitter;
import lombok.EqualsAndHashCode;

/**
 * Represents addition of Transmitter to the pool.
 */
@EqualsAndHashCode(callSuper = true)
public class AdditionEvent extends TransmitterPoolEvent {

    public AdditionEvent(Transmitter transmitter) {
        super(transmitter, Operation.ADD);
    }
}
