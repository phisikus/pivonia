package eu.phisikus.pivonia.pool.server;

import eu.phisikus.pivonia.api.Server;
import lombok.Value;

@Value
public class ServerEvent {
    private final Server server;
    private final Operation operation;

    public enum Operation {
        ADD, REMOVE
    }
}
