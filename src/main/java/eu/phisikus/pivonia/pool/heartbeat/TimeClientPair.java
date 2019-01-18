package eu.phisikus.pivonia.pool.heartbeat;

import eu.phisikus.pivonia.api.Client;
import lombok.Value;

@Value
class TimeClientPair {
    private final Long lastSeen;
    private final Client client;
}
