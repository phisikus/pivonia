package eu.phisikus.pivonia.pool.heartbeat.events;

import eu.phisikus.pivonia.api.Client;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * It represents heartbeat being received from node of given ID through provided Client.
 *
 * @param <K> type of node ID
 */
@EqualsAndHashCode(callSuper = true)
public class ReceivedEvent<K> extends HeartbeatPoolEvent {

    @Getter
    private final K id;

    public ReceivedEvent(K id, Client client) {
        super(client, Operation.RECEIVED);
        this.id = id;
    }
}
