package eu.phisikus.pivonia.pool.heartbeat;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.MessageWithClient;
import eu.phisikus.pivonia.pool.HeartbeatPool;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Log4j2
public class HeartbeatPoolImpl<K> implements HeartbeatPool<K>, AutoCloseable {
    private final ScheduledExecutorService heartbeatSender = Executors.newSingleThreadScheduledExecutor();
    private final List<HeartbeatEntry> clients = Collections.synchronizedList(new LinkedList<>());
    private final Subject<HeartbeatEvent<K>> heartbeatChanges = PublishSubject.create();
    private final K nodeId;
    private final long neverSeen = 0L;
    private final long timeoutDelay;

    public HeartbeatPoolImpl(Long heartbeatDelay, long timeoutDelay, K nodeId) {
        this.nodeId = nodeId;
        this.timeoutDelay = timeoutDelay;
        heartbeatSender.scheduleWithFixedDelay(
                getHeartbeatSenderTask(), 0L, heartbeatDelay, TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void add(Client client) {
        var subscription = client
                .getMessages(HeartbeatMessage.class)
                .subscribe(getHeartbeatMessageHandler());
        clients.add(new HeartbeatEntry(false, neverSeen, client, subscription));
    }

    private Consumer<MessageWithClient<HeartbeatMessage>> getHeartbeatMessageHandler() {
        return heartbeatMessageMessageWithClient -> {
            var message = heartbeatMessageMessageWithClient.getMessage();
            var client = heartbeatMessageMessageWithClient.getClient();

            log.trace("Received heartbeat message: {}", message);
            if (message.getTimestamp() > 0L) {
                processHeartbeatResponse(message, client);
            } else {
                sendHeartbeatResponse(client);
            }

        };
    }

    private void sendHeartbeatResponse(Client client) {
        client.send(new HeartbeatMessage<>(nodeId, getCurrentTimestamp()));
    }

    private void processHeartbeatResponse(HeartbeatMessage message, Client client) {
        getEntriesForClient(client)
                .forEach(entry -> {
                    entry.setLastSeen(getCurrentTimestamp());
                    sendReceivedEvent((K) message.getSenderId(), client);
                });

    }

    private void sendReceivedEvent(K senderId, Client client) {
        var event = new HeartbeatEvent<>(
                senderId,
                client,
                HeartbeatEvent.Operation.RECEIVED
        );
        log.info("Emitting RECEIVED event: {}", event);
        heartbeatChanges.onNext(event);
    }

    @Override
    public void remove(Client client) {
        getEntriesForClient(client)
                .forEach(heartbeatEntry -> {
                    heartbeatEntry.getSubscription().dispose();
                    clients.remove(heartbeatEntry);
                });
    }

    @Override
    public Observable<HeartbeatEvent<K>> getHeartbeatChanges() {
        return heartbeatChanges;
    }

    private Long getCurrentTimestamp() {
        return Instant.now().toEpochMilli();
    }


    private Runnable getHeartbeatSenderTask() {
        return this::processCurrentQueue;
    }

    private void processCurrentQueue() {
        log.trace("Executing periodical heartbeat loop...");
        clients.forEach(this::handleHeartbeatForClient);
        log.trace("Heartbeat loop complete.");
    }

    private void handleHeartbeatForClient(HeartbeatEntry heartbeatEntry) {
        var isTimeoutClient = getCurrentTimestamp() - heartbeatEntry.getLastSeen() > timeoutDelay &&
                heartbeatEntry.getWasHeartbeatSent();

        if (isTimeoutClient) {
            handleClientTimeout(heartbeatEntry.getClient());
            return;
        }

        heartbeatEntry.setWasHeartbeatSent(true);
        sendHeartbeat(heartbeatEntry.getClient());
    }

    private void handleClientTimeout(Client client) {
        var timeoutEvent = new HeartbeatEvent<K>(null, client, HeartbeatEvent.Operation.TIMEOUT);
        log.info("Emitting TIMEOUT event: {}", timeoutEvent);
        clients.removeIf(entry -> client.equals(entry.getClient()));
        heartbeatChanges.onNext(timeoutEvent);
    }

    private void sendHeartbeat(Client client) {
        var message = new HeartbeatMessage<>(nodeId, neverSeen);
        log.trace("Sending heartbeat message: {}", message);
        client.send(message);
    }

    private Stream<HeartbeatEntry> getEntriesForClient(Client client) {
        Predicate<HeartbeatEntry> entriesContainClient = entry -> entry.getClient().equals(client);
        return clients.stream()
                .filter(entriesContainClient);
    }

    @Override
    public void close() throws Exception {
        log.info("Closing heartbeat pool.");
        heartbeatSender.shutdownNow();
        clients.clear();
    }
}