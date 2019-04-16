package eu.phisikus.pivonia.pool.transmitter.events;

import eu.phisikus.pivonia.api.Transmitter;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public abstract class TransmitterPoolEvent {

    @Getter
    protected final Transmitter transmitter;
    @Getter
    protected final Operation operation;

    TransmitterPoolEvent(Transmitter transmitter, Operation operation) {
        this.transmitter = transmitter;
        this.operation = operation;
    }

    public enum Operation {
        ADD, REMOVE, ASSIGN, UNASSIGN
    }
}
