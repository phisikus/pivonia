package eu.phisikus.pivonia.pool.heartbeat;

import eu.phisikus.pivonia.api.Client;
import io.reactivex.disposables.Disposable;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class ClientHeartbeatEntry {
    private Boolean wasHeartbeatSent;
    private Long lastSeen;
    private Client client;
    private Disposable subscription;
}
