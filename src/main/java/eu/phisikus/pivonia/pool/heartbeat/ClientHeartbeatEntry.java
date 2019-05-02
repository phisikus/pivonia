package eu.phisikus.pivonia.pool.heartbeat;

import eu.phisikus.pivonia.api.Client;
import io.reactivex.disposables.Disposable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class ClientHeartbeatEntry {
    private Boolean wasHeartbeatSent;
    private Long lastSeen;
    private Client client;
    private Disposable subscription;
}
