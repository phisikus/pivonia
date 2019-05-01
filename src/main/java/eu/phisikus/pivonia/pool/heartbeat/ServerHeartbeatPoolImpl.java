package eu.phisikus.pivonia.pool.heartbeat;

import eu.phisikus.pivonia.api.MessageWithTransmitter;
import eu.phisikus.pivonia.api.Server;
import eu.phisikus.pivonia.pool.ServerHeartbeatPool;
import io.reactivex.disposables.Disposable;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerHeartbeatPoolImpl<K> implements ServerHeartbeatPool<K> {
    @Getter
    private final List<Server> servers = new CopyOnWriteArrayList<>();
    private final Map<Server, Disposable> listeners = new ConcurrentHashMap<>();
    private final K nodeId;

    public ServerHeartbeatPoolImpl(K nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public void add(Server server) {
        var listener = registerHeartbeatListener(nodeId, server);
        listeners.put(server, listener);
        servers.add(server);
    }

    private Disposable registerHeartbeatListener(K nodeId, Server server) {
        return server.getMessages(HeartbeatMessage.class)
                .subscribe(event -> sendResponse(nodeId, event));
    }

    private void sendResponse(K nodeId, MessageWithTransmitter<HeartbeatMessage> event) {
        var transmitter = event.getTransmitter();
        var currentTime = Instant.now().toEpochMilli();
        var response = new HeartbeatMessage<>(nodeId, currentTime);
        transmitter.send(response);
    }

    @Override
    public void remove(Server server) {
        var listener = listeners.remove(server);
        if (listener != null) {
            listener.dispose();
        }
        servers.remove(server);
    }

}
