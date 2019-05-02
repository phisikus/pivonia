package eu.phisikus.pivonia.pool.heartbeat;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.MessageWithTransmitter;
import eu.phisikus.pivonia.api.Transmitter;
import eu.phisikus.pivonia.pool.ClientHeartbeatPool;
import eu.phisikus.pivonia.pool.heartbeat.events.HeartbeatPoolEvent;
import eu.phisikus.pivonia.pool.heartbeat.events.ReceivedEvent;
import eu.phisikus.pivonia.pool.heartbeat.events.TimeoutEvent;
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
class ClientHeartbeatPoolImpl<K> implements ClientHeartbeatPool<K>, AutoCloseable {
    private final ScheduledExecutorService heartbeatSender = Executors.newSingleThreadScheduledExecutor();
    private final List<ClientHeartbeatEntry> clients = Collections.synchronizedList(new LinkedList<>());
    private final Subject<HeartbeatPoolEvent> heartbeatChanges = PublishSubject.create();
    private final K nodeId;
    private final long neverSeen = 0L;
    private final long timeoutDelay;

    public ClientHeartbeatPoolImpl(long heartbeatDelay, long timeoutDelay, K nodeId) {
        this.nodeId = nodeId;
        this.timeoutDelay = timeoutDelay;
        heartbeatSender.scheduleWithFixedDelay(
                getHeartbeatSenderTask(), heartbeatDelay, heartbeatDelay, TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void add(Client client) {
        var subscription = client
                .getMessages(HeartbeatMessage.class)
                .subscribe(getHeartbeatMessageHandler());
        clients.add(new ClientHeartbeatEntry(false, neverSeen, client, subscription));
    }

    private Consumer<MessageWithTransmitter<HeartbeatMessage>> getHeartbeatMessageHandler() {
        return heartbeatMessageMessageWithClient -> {
            var message = heartbeatMessageMessageWithClient.getMessage();
            var transmitter = heartbeatMessageMessageWithClient.getTransmitter();

            log.trace("Received heartbeat message: {}", message);
            if (message.getTimestamp() > 0L) {
                processHeartbeatResponse(message, transmitter);
            } else {
                sendHeartbeatResponse(transmitter);
            }

        };
    }

    private void sendHeartbeatResponse(Transmitter transmitter) {
        transmitter.send(new HeartbeatMessage<>(nodeId, getCurrentTimestamp()));
    }

    private void processHeartbeatResponse(HeartbeatMessage message, Transmitter transmitter) {
        getEntriesForClient((Client) transmitter)
                .forEach(entry -> {
                    entry.setLastSeen(getCurrentTimestamp());
                    sendReceivedEvent((K) message.getSenderId(), (Client) transmitter);
                });

    }

    private void sendReceivedEvent(K senderId, Client client) {
        var event = new ReceivedEvent<>(senderId, client);
        log.info("Emitting RECEIVED event: {}", event);
        heartbeatChanges.onNext(event);
    }

    @Override
    public void remove(Client client) {
        getEntriesForClient(client)
                .forEach(clientHeartbeatEntry -> {
                    clientHeartbeatEntry.getSubscription().dispose();
                    clients.remove(clientHeartbeatEntry);
                });
    }

    @Override
    public Observable<HeartbeatPoolEvent> getHeartbeatChanges() {
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

    private void handleHeartbeatForClient(ClientHeartbeatEntry clientHeartbeatEntry) {
        var isTimeoutClient = getCurrentTimestamp() - clientHeartbeatEntry.getLastSeen() > timeoutDelay &&
                clientHeartbeatEntry.getWasHeartbeatSent();

        if (isTimeoutClient) {
            handleClientTimeout(clientHeartbeatEntry.getClient());
            return;
        }

        clientHeartbeatEntry.setWasHeartbeatSent(true);
        sendHeartbeat(clientHeartbeatEntry.getClient());
    }

    private void handleClientTimeout(Client client) {
        var timeoutEvent = new TimeoutEvent(client);
        log.info("Emitting TIMEOUT event: {}", timeoutEvent);
        clients.removeIf(entry -> client.equals(entry.getClient()));
        heartbeatChanges.onNext(timeoutEvent);
    }

    private void sendHeartbeat(Transmitter transmitter) {
        var message = new HeartbeatMessage<>(nodeId, neverSeen);
        log.trace("Sending heartbeat message: {}", message);
        transmitter.send(message);
    }

    private Stream<ClientHeartbeatEntry> getEntriesForClient(Client client) {
        Predicate<ClientHeartbeatEntry> entriesContainClient = entry -> entry.getClient().equals(client);
        var clientsCopy = clients.toArray(new ClientHeartbeatEntry[0]);
        return Stream.of(clientsCopy)
                .filter(entriesContainClient);
    }

    @Override
    public void close() {
        log.info("Closing client heartbeat pool.");
        heartbeatSender.shutdownNow();
        clients.clear();
    }
}
