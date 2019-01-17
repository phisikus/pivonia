package eu.phisikus.pivonia.pool.client;

import eu.phisikus.pivonia.api.Client;
import lombok.Value;

@Value
public class ClientChange {

    private final Client client;
    private final Operation operation;

    public enum Operation {
        ADD, REMOVE
    }
}
