package eu.phisikus.pivonia.pool.heartbeat;

import eu.phisikus.pivonia.api.Client;
import io.reactivex.disposables.Disposable;
import lombok.Value;

@Value
class HeartbeatEntry {
    private final Long lastSeen;
    private final Client client;
    private final Disposable subscription;
}
