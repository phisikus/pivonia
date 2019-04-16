package eu.phisikus.pivonia.pool.transmitter.events;

import eu.phisikus.pivonia.api.Transmitter;
import lombok.EqualsAndHashCode;

/**
 * Represents removal of Transmitter from the pool.
 */
@EqualsAndHashCode(callSuper = true)
public class RemovalEvent extends TransmitterPoolEvent {

    public RemovalEvent(Transmitter transmitter) {
        super(transmitter, Operation.REMOVE);
    }
}
