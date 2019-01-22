package eu.phisikus.pivonia.pool.heartbeat;

import eu.phisikus.pivonia.api.Client;
import io.reactivex.disposables.Disposable;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
class HeartbeatEntry {
    private Boolean wasHeartbeatSent;
    private Long lastSeen;
    private Client client;
    private Disposable subscription;
}
