package eu.phisikus.pivonia.pool.client;

import eu.phisikus.pivonia.api.Client;
import lombok.Value;

@Value
public class ClientChange<K> {

    private final Client client;
    private final K id;
    private final Operation operation;

    public enum Operation {
        ADD, REMOVE, ASSIGN, UNASSIGN
    }
}
