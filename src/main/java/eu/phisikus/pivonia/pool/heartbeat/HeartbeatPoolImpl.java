package eu.phisikus.pivonia.pool.heartbeat;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.MessageWithClient;
import eu.phisikus.pivonia.pool.HeartbeatPool;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartbeatPoolImpl<K> implements HeartbeatPool<K> {
    private final ScheduledExecutorService heartbeatSender = Executors.newSingleThreadScheduledExecutor();
    private final Queue<TimeClientPair> clients = new ConcurrentLinkedQueue<>();
    private final Subject<HeartbeatEvent<K>> heartbeatChanges = PublishSubject.create();
    private final K nodeId;
    private final long neverSeen = 0L;
    private final int timeoutDelay;

    public HeartbeatPoolImpl(Long heartbeatDelay, int timeoutDelay, K nodeId) {
        this.nodeId = nodeId;
        this.timeoutDelay = timeoutDelay;
        heartbeatSender.scheduleWithFixedDelay(
                getHeartbeatSenderTask(), 0L, heartbeatDelay, TimeUnit.SECONDS
        );
    }

    @Override
    public void add(Client client) {
        client.getMessages(HeartbeatMessage.class).subscribe(getHeartbeatMessageHandler());
        clients.offer(new TimeClientPair(neverSeen, client));
    }

    private Consumer<MessageWithClient<HeartbeatMessage>> getHeartbeatMessageHandler() {
        return heartbeatMessageMessageWithClient -> {
            var message = heartbeatMessageMessageWithClient.getMessage();
            var client = heartbeatMessageMessageWithClient.getClient();
            var event = new HeartbeatEvent<>(
                    (K) message.getSenderId(),
                    client,
                    HeartbeatEvent.Operation.RECEIVED
            );
            heartbeatChanges.onNext(event);
        };
    }

    @Override
    public void remove(Client client) {
        clients.remove(client);
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
            while (!clients.isEmpty()) {
                var clientWithTime = clients.poll();
                if (clientWithTime != null) {
                    handleHeartbeatForClient(clientWithTime);
                }
            }
        };
    }

    private void handleHeartbeatForClient(TimeClientPair timeClientPair) {
        var isTimeoutClient = getCurrentTimestamp() - timeClientPair.getLastSeen() > timeoutDelay &&
                timeClientPair.getLastSeen() != neverSeen;

        if (isTimeoutClient) {
            handleClientTimeout(timeClientPair.getClient());
            return;
        }

        clients.offer(timeClientPair);
        sendHeartbeat(timeClientPair.getClient());
    }

    private void handleClientTimeout(Client client) {
        var timeoutEvent = new HeartbeatEvent<K>(null, client, HeartbeatEvent.Operation.TIMEOUT);
        heartbeatChanges.onNext(timeoutEvent);
    }

    private void sendHeartbeat(Client client) {
        var message = new HeartbeatMessage<>(nodeId, null);
        client.send(message);
    }
}
