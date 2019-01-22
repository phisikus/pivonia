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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@Log4j2
public class HeartbeatPoolImpl<K> implements HeartbeatPool<K>, AutoCloseable {
    private final ScheduledExecutorService heartbeatSender = Executors.newSingleThreadScheduledExecutor();
    private final Queue<HeartbeatEntry> clients = new ConcurrentLinkedQueue<>();
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
        clients.offer(new HeartbeatEntry(neverSeen, client, subscription));
    }

    private Consumer<MessageWithClient<HeartbeatMessage>> getHeartbeatMessageHandler() {
        return heartbeatMessageMessageWithClient -> {
            var message = heartbeatMessageMessageWithClient.getMessage();
            var client = heartbeatMessageMessageWithClient.getClient();

            log.info("Processing heartbeat message: {}", message);
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
        Predicate<HeartbeatEntry> entriesContainClient = entry -> entry.getClient().equals(client);

        clients.stream()
                .filter(entriesContainClient)
                .forEach(entry -> {
                    clients.remove(entry);
                    var newEntry = new HeartbeatEntry(
                            getCurrentTimestamp(),
                            client,
                            entry.getSubscription()
                    );
                    sendReceivedEvent((K) message.getSenderId(), client);
                    clients.offer(newEntry);
                });
    }

    private void sendReceivedEvent(K senderId, Client client) {
        var event = new HeartbeatEvent<>(
                (K) senderId,
                client,
                HeartbeatEvent.Operation.RECEIVED
        );
        heartbeatChanges.onNext(event);
    }

    @Override
    public void remove(Client client) {
        clients.stream()
                .filter(heartbeatEntry -> heartbeatEntry.getClient().equals(client))
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
        return () -> {
            var processedClients = processCurrentQueue();
            processedClients.forEach(clients::offer);
        };
    }

    private List<HeartbeatEntry> processCurrentQueue() {
        var processedClients = new LinkedList<HeartbeatEntry>();
        while (!clients.isEmpty()) {
            var clientWithTime = clients.poll();
            if (clientWithTime != null) {
                handleHeartbeatForClient(clientWithTime, processedClients);
            }
        }
        return processedClients;
    }

    private void handleHeartbeatForClient(HeartbeatEntry heartbeatEntry, List<HeartbeatEntry> processedClients) {
        var isTimeoutClient = getCurrentTimestamp() - heartbeatEntry.getLastSeen() > timeoutDelay &&
                heartbeatEntry.getLastSeen() != neverSeen;

        if (isTimeoutClient) {
            handleClientTimeout(heartbeatEntry.getClient());
            return;
        }

        processedClients.add(heartbeatEntry);
        sendHeartbeat(heartbeatEntry.getClient());
    }

    private void handleClientTimeout(Client client) {
        var timeoutEvent = new HeartbeatEvent<K>(null, client, HeartbeatEvent.Operation.TIMEOUT);
        log.info("Sending timeout event: {}", timeoutEvent);
        heartbeatChanges.onNext(timeoutEvent);
    }

    private void sendHeartbeat(Client client) {
        var message = new HeartbeatMessage<>(nodeId, null);
        log.info("Sending heartbeat message: {}", message);
        client.send(message);
    }

    @Override
    public void close() throws Exception {
        log.info("Closing heartbeat pool.");
        heartbeatSender.shutdownNow();
        clients.clear();
    }
}
