package eu.phisikus.pivonia.pool.transmitter;

import eu.phisikus.pivonia.api.Transmitter;
import lombok.Value;

@Value
public class TransmitterPoolEvent<K> {

    private final Transmitter transmitter;
    private final K id;
    private final Operation operation;

    public enum Operation {
        ADD, REMOVE, ASSIGN, UNASSIGN
    }
}
