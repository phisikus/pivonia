package eu.phisikus.pivonia.pool.heartbeat;

import eu.phisikus.pivonia.api.MessageWithTransmitter;
import eu.phisikus.pivonia.api.Server;
import io.reactivex.disposables.Disposable;

import java.time.Instant;

public class HeartbeatServerVisitor {

    /**
     * Registers heartbeat algorithm listener that responds to heartbeat requests.
     *
     * @param nodeId ID that will be used to identify the server in outgoing responses
     * @param server server instance used to register heartbeat listener
     * @param <K>    type of node ID
     * @return disposable subscription
     */
    public static <K> Disposable registerHeartbeatListener(K nodeId, Server server) {
        return server.getMessages(HeartbeatMessage.class)
                .subscribe(event -> sendResponse(nodeId, event));
    }

    private static <K> void sendResponse(K nodeId, MessageWithTransmitter<HeartbeatMessage> event) {
        var client = event.getClient();
        var currentTime = Instant.now().toEpochMilli();
        var response = new HeartbeatMessage<>(nodeId, currentTime);
        client.send(response);
    }
}
