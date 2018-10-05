package eu.phisikus.pivonia.pool.health;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.MessageHandler;
import eu.phisikus.pivonia.pool.ClientPool;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This Client Pool checks if all of the clients are alive by sending messages periodically.
 * Those messages are not passed to regular message handlers but used to judge if client should be reconnected.
 * Once client misses certain number of "heartbeat" messages it is considered dead and any ID -> Client mapping is removed.
 *
 * @param <K> type of key used to ID the nodes
 * @param <T> type of message being passed between the Clients
 */
@Log4j2
public class ClientPoolWithHealthCheck<K, T> implements ClientPool<K> {
    /**
     * Number of lost echo messages that cause invalidation of ID -> Client mapping
     */
    private static final int MAX_LOST_ECHOS = 3;

    /**
     * Reconnection of the client will be performed exponentially less often based on this value
     */
    private static final int RECONNECT_POWER_BASE = 4;

    private final K nodeID;
    private final EchoMessageFactory<K, T> echoMessageFactory;
    private final MessageHandler<T> targetMessageHandler;
    private final Long timeRate;

    private final Map<K, Client> clientsForNodeId = new ConcurrentHashMap<>();
    private final List<ClientHealthEntry<T>> clientHealthEntries = new LinkedList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);


    /**
     *
     * @param nodeID node ID
     * @param timeRate amount of time (in milliseconds) between health checks messages
     * @param echoMessageFactory factory of messages used for health checks
     * @param messageHandler common message handler for all of the clients
     */
    public ClientPoolWithHealthCheck(K nodeID,
                                     Long timeRate,
                                     EchoMessageFactory<K, T> echoMessageFactory,
                                     MessageHandler<T> messageHandler) {
        this.nodeID = nodeID;
        this.echoMessageFactory = echoMessageFactory;
        this.targetMessageHandler = messageHandler;
        this.timeRate = timeRate;
    }


    @Override
    public Optional<Client> getClient(K id) {
        return Optional.ofNullable(clientsForNodeId.get(id));
    }

    @Override
    public boolean isAvailable(K id) {
        return clientsForNodeId.containsKey(id);
    }


    @Override
    public void close() throws Exception {
        scheduler.shutdownNow();
        Consumer<Client> closeClient = client -> {
            try {
                client.close();
            } catch (Exception exception) {
                log.error(exception);
            }
        };

        clientHealthEntries.stream()
                .map(ClientHealthEntry::getCurrentClient)
                .forEach(closeClient);
    }

    /**
     * Add pre-configured Client, so it can be built using provided builder.
     * This client pool provides the MessageHandler that will react to heart beat messages.
     * All other messages will be forwarded to the "real" message handler provided in the constructor.
     *
     * @param clientBuilder client builder that will be called and result added to the pool
     */
    public void addClient(Function<MessageHandler<T>, Client> clientBuilder) {
        synchronized (clientHealthEntries) {
            var healthEntry = buildInitialHealthEntry(clientBuilder);
            var healthCheckMessageHandler = buildHealthCheckMessageHandler(healthEntry);
            var newClient = clientBuilder.apply(healthCheckMessageHandler);
            healthEntry.setCurrentClient(newClient);
            scheduler.scheduleAtFixedRate(getHealthCheckProcedure(healthEntry), 0L, timeRate, TimeUnit.MILLISECONDS);
        }
    }

    private Runnable getHealthCheckProcedure(ClientHealthEntry<T> healthEntry) {
        return () -> {
            T pingMessage = echoMessageFactory.getMessage(nodeID);
            var currentTime = Instant.now().toEpochMilli();
            var lastTimeSeen = healthEntry.getLastTimeSeen();
            var currentClient = healthEntry.getCurrentClient();
            if (isTimeout(currentTime, lastTimeSeen)) {
                removeClientFromMapping(currentClient, clientsForNodeId);
                currentClient = getReconnectedClientIfTimeout(currentTime, lastTimeSeen, healthEntry);
            }
            currentClient.send(pingMessage);
        };
    }

    private void removeClientFromMapping(Client currentClient, Map<K, Client> clientsForNodeId) {
        clientsForNodeId
                .entrySet()
                .stream()
                .filter(entry -> currentClient.equals(entry.getValue()))
                .findAny()
                .ifPresent(entry -> clientsForNodeId.remove(entry.getKey(), entry.getValue()));
    }

    private boolean isTimeout(long currentTime, Long lastTimeSeen) {
        return (currentTime - lastTimeSeen) > timeRate * MAX_LOST_ECHOS;
    }

    private Client getReconnectedClientIfTimeout(Long currentTime, Long lastTimeSeen, ClientHealthEntry<T> healthEntry) {
        var reconnectAttempts = healthEntry.getReconnectAttempts();
        if (isReconnectNeeded(currentTime, lastTimeSeen, reconnectAttempts)) {
            closeCurrentClient(healthEntry);
            return createAndSetNewClient(healthEntry);
        }
        return healthEntry.getCurrentClient();
    }

    private Client createAndSetNewClient(ClientHealthEntry<T> healthEntry) {
        var newClient = healthEntry.getClientBuilder().apply(buildHealthCheckMessageHandler(healthEntry));
        var reconnectAttempts = healthEntry.getReconnectAttempts();
        healthEntry.setCurrentClient(newClient);
        healthEntry.setReconnectAttempts(reconnectAttempts + 1);
        return newClient;

    }

    private void closeCurrentClient(ClientHealthEntry<T> healthEntry) {
        try {
            healthEntry.getCurrentClient().close();
        } catch (Exception exception) {
            log.error(exception);
        }
    }

    private boolean isReconnectNeeded(Long currentTime, Long lastTimeSeen, Long reconnectAttempts) {
        var passedTime = currentTime - lastTimeSeen;
        var cyclesLost = Math.floor(passedTime * 1.0d / timeRate);
        return cyclesLost > Math.pow(RECONNECT_POWER_BASE, reconnectAttempts);
    }

    private HealthCheckMessageHandler<K, T> buildHealthCheckMessageHandler(ClientHealthEntry<T> healthEntry) {
        return HealthCheckMessageHandler.<K, T>builder()
                .nodeId(nodeID)
                .clientForNode(clientsForNodeId)
                .messageFactory(echoMessageFactory)
                .targetMessageHandler(targetMessageHandler)
                .healthEntry(healthEntry)
                .build();
    }

    private ClientHealthEntry<T> buildInitialHealthEntry(Function<MessageHandler<T>, Client> clientBuilder) {
        return ClientHealthEntry.<T>builder()
                .clientBuilder(clientBuilder)
                .reconnectAttempts(0L)
                .lastTimeSeen(0L)
                .build();
    }

}
