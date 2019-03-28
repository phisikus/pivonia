package eu.phisikus.pivonia.pool.heartbeat;

import eu.phisikus.pivonia.api.MessageWithClient;
import eu.phisikus.pivonia.api.Server;
import io.reactivex.disposables.Disposable;

import java.time.Instant;

public class HeartbeatServerVisitor {
    public static <K> Disposable registerHeartbeatListener(K nodeId, Server server) {
        return server.getMessages(HeartbeatMessage.class)
                .subscribe(event -> sendResponse(nodeId, event));
    }

    private static <K> void sendResponse(K nodeId, MessageWithClient<HeartbeatMessage> event) {
        var client = event.getClient();
        var currentTime = Instant.now().toEpochMilli();
        var response = new HeartbeatMessage<>(nodeId, currentTime);
        client.send(response);
    }
}
