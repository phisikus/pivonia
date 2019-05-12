package eu.phisikus.pivonia.pool.heartbeat;

import eu.phisikus.pivonia.api.MessageWithTransmitter;
import eu.phisikus.pivonia.api.Server;
import eu.phisikus.pivonia.api.Transmitter;
import eu.phisikus.pivonia.pool.ServerHeartbeatPool;
import eu.phisikus.pivonia.pool.heartbeat.events.HeartbeatPoolEvent;
import eu.phisikus.pivonia.pool.heartbeat.events.ReceivedEvent;
import eu.phisikus.pivonia.pool.heartbeat.events.TimeoutEvent;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Log4j2
class ServerHeartbeatPoolImpl<K> implements ServerHeartbeatPool<K> {
    @Getter
    private final List<Server> servers = new CopyOnWriteArrayList<>();
    private final List<ServerHeartbeatEntry> clients = Collections.synchronizedList(new LinkedList<>());
    private final ScheduledExecutorService timeoutSender = Executors.newSingleThreadScheduledExecutor();
    private final Map<Server, Disposable> listeners = new ConcurrentHashMap<>();
    private final Subject<HeartbeatPoolEvent> poolChanges = PublishSubject.create();
    private final K nodeId;
    private long timeoutDelay;

    public ServerHeartbeatPoolImpl(long heartbeatDelay, long timeoutDelay, K nodeId) {
        this.timeoutDelay = timeoutDelay;
        this.nodeId = nodeId;
        timeoutSender.scheduleWithFixedDelay(getTimeoutTask(), heartbeatDelay, heartbeatDelay, TimeUnit.MILLISECONDS);
    }

    private Runnable getTimeoutTask() {
        return () -> {
            log.trace("Executing periodical heartbeat loop...");
            clients.forEach(this::checkAndSendTimeout);
            log.trace("Heartbeat loop complete.");
        };
    }

    private void checkAndSendTimeout(ServerHeartbeatEntry serverHeartbeatEntry) {
        var currentTime = Instant.now().toEpochMilli();
        var isTimeout = currentTime - serverHeartbeatEntry.getLastSeen() > timeoutDelay;
        if (isTimeout) {
            clients.remove(serverHeartbeatEntry);
            removeListener(serverHeartbeatEntry.getServer());
            sendTimeoutEvent(serverHeartbeatEntry);
        }
    }

    private void sendTimeoutEvent(ServerHeartbeatEntry serverHeartbeatEntry) {
        var timeoutEvent = new TimeoutEvent(serverHeartbeatEntry.getTransmitter());
        log.info("Emitting TIMEOUT event: {}", timeoutEvent);
        poolChanges.onNext(timeoutEvent);
    }

    @Override
    public void add(Server server) {
        var listener = registerHeartbeatListener(nodeId, server);
        listeners.put(server, listener);
        servers.add(server);
    }

    private Disposable registerHeartbeatListener(K nodeId, Server server) {
        return server.getMessages(HeartbeatMessage.class)
                .subscribe(event -> sendResponse(nodeId, event, server));
    }

    private void sendResponse(K nodeId, MessageWithTransmitter<HeartbeatMessage> event, Server server) {
        var transmitter = event.getTransmitter();
        var message = event.getMessage();
        var currentTime = Instant.now().toEpochMilli();
        log.trace("Received heartbeat message: {}", message);
        saveHeartbeatReceptionEntry(currentTime, server, event.getTransmitter());
        sendHeartbeatResponse(currentTime, nodeId, transmitter);
        sendReceivedEvent(transmitter, message.getSenderId());
    }

    private void sendHeartbeatResponse(long currentTime, K nodeId, Transmitter transmitter) {
        var response = new HeartbeatMessage<>(nodeId, currentTime);
        log.trace("Sending heartbeat response: {}", response);
        transmitter.send(response);
    }

    private void saveHeartbeatReceptionEntry(long currentTime, Server server, Transmitter transmitter) {
        var entry = getClientEntry(currentTime, server, transmitter);
        entry.setLastSeen(currentTime);
    }

    private ServerHeartbeatEntry getClientEntry(long currentTime, Server server, Transmitter transmitter) {
        return clients.stream()
                .filter(serverHeartbeatEntry -> serverHeartbeatEntry.getTransmitter().equals(transmitter))
                .findAny()
                .orElseGet(() -> addNewEntry(currentTime, server, transmitter));
    }

    private ServerHeartbeatEntry addNewEntry(long currentTime, Server server, Transmitter transmitter) {
        var newEntry = new ServerHeartbeatEntry(currentTime, transmitter, server);
        clients.add(newEntry);
        return newEntry;
    }

    private void sendReceivedEvent(Transmitter transmitter, Object senderId) {
        var receivedEvent = new ReceivedEvent<>(senderId, transmitter);
        log.debug("Emitting RECEIVED event: {}", receivedEvent);
        poolChanges.onNext(receivedEvent);
    }

    @Override
    public void remove(Server server) {
        removeListener(server);
        servers.remove(server);
    }

    @Override
    public Observable<HeartbeatPoolEvent> getHeartbeatChanges() {
        return poolChanges;
    }

    private void removeListener(Server server) {
        var listener = listeners.remove(server);
        if (listener != null) {
            listener.dispose();
        }
    }

}
