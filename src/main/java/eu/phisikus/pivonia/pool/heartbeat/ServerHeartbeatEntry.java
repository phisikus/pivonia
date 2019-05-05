package eu.phisikus.pivonia.pool.heartbeat;

import eu.phisikus.pivonia.api.Server;
import eu.phisikus.pivonia.api.Transmitter;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class ServerHeartbeatEntry {
    private Long lastSeen;
    private Transmitter transmitter;
    private Server server;
}
