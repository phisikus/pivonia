package eu.phisikus.pivonia.pool.heartbeat.events;

import eu.phisikus.pivonia.api.Transmitter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * It represents timeout event for given Client instance.
 */
@EqualsAndHashCode(callSuper = true)
@ToString
public class TimeoutEvent extends HeartbeatPoolEvent {
    public TimeoutEvent(Transmitter transmitter) {
        super(transmitter, Operation.TIMEOUT);
    }
}
