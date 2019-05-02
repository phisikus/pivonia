package eu.phisikus.pivonia.pool.heartbeat;

import eu.phisikus.pivonia.api.Transmitter;
import io.reactivex.disposables.Disposable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class HeartbeatEntry {
    private Boolean wasHeartbeatSent;
    private Long lastSeen;
    private Transmitter transmitter;
    private Disposable subscription;
}
