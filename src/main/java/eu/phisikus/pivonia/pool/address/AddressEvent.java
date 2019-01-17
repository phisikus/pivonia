package eu.phisikus.pivonia.pool.address;

import lombok.Value;

@Value
public class AddressEvent {

    private final Operation operation;
    private final Address address;

    public enum Operation {
        ADD, REMOVE
    }
}
