package eu.phisikus.pivonia.pool.heartbeat.events;

import eu.phisikus.pivonia.api.Client;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public abstract class HeartbeatPoolEvent {

    @Getter
    private final Client client;

    @Getter
    private final Operation operation;

    HeartbeatPoolEvent(Client client, Operation operation) {
        this.client = client;
        this.operation = operation;
    }

    public enum Operation {
        RECEIVED, TIMEOUT
    }
}
