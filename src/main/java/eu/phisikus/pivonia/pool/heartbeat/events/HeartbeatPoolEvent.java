package eu.phisikus.pivonia.pool.heartbeat.events;

import eu.phisikus.pivonia.api.Transmitter;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public abstract class HeartbeatPoolEvent {

    @Getter
    private final Transmitter transmitter;

    @Getter
    private final Operation operation;

    HeartbeatPoolEvent(Transmitter transmitter, Operation operation) {
        this.transmitter = transmitter;
        this.operation = operation;
    }

    public enum Operation {
        RECEIVED, TIMEOUT
    }
}
