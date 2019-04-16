package eu.phisikus.pivonia.pool.heartbeat.events;

import eu.phisikus.pivonia.api.Client;
import lombok.EqualsAndHashCode;

/**
 * It represents timeout event for given Client instance.
 */
@EqualsAndHashCode(callSuper = true)
public class TimeoutEvent extends HeartbeatPoolEvent {
    public TimeoutEvent(Client client) {
        super(client, Operation.TIMEOUT);
    }
}
