package eu.phisikus.pivonia.pool.server;

import eu.phisikus.pivonia.api.Server;
import lombok.Value;

@Value
public class ServerPoolEvent {
    private final Server server;
    private final Operation operation;

    public enum Operation {
        ADD, REMOVE
    }
}
