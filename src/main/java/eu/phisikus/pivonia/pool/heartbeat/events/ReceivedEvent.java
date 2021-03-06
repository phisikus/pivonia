package eu.phisikus.pivonia.pool.heartbeat.events;

import eu.phisikus.pivonia.api.Transmitter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * It represents heartbeat being received from node of given ID through provided Client.
 *
 * @param <K> type of node ID
 */
@EqualsAndHashCode(callSuper = true)
@ToString
public class ReceivedEvent<K> extends HeartbeatPoolEvent {

    @Getter
    private final K id;

    public ReceivedEvent(K id, Transmitter transmitter) {
        super(transmitter, Operation.RECEIVED);
        this.id = id;
    }
}
