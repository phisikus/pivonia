package eu.phisikus.pivonia.pool.health;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.MessageHandler;
import eu.phisikus.pivonia.pool.ClientPool;
import lombok.extern.log4j.Log4j2;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

@Log4j2
public class ClientPoolWithHealthCheck<K, T> implements ClientPool<K> {
    private final K nodeID;
    private final EchoMessageFactory<K, T> echoMessageFactory;
    private final MessageHandler<T> targetMessageHandler;
    private final Long timeRate;

    private final Map<K, Client> clientsForNodeId = new ConcurrentHashMap<>();
    private final List<ClientHealthEntry<T>> clientHealthEntries = new LinkedList<>();


    public ClientPoolWithHealthCheck(K nodeID, Long timeRate, EchoMessageFactory<K, T> echoMessageFactory, MessageHandler<T> messageHandler) {
        this.nodeID = nodeID;
        this.echoMessageFactory = echoMessageFactory;
        this.targetMessageHandler = messageHandler;
        this.timeRate = timeRate;
        // TODO add thread that goes through clientHealth Entires and sends echo!
    }


    @Override
    public Optional<Client> getClient(K id) {
        return Optional.ofNullable(clientsForNodeId.get(id));
    }

    @Override
    public boolean isAvailable(K id) {
        return clientsForNodeId.containsKey(id);
    }

    void addClient(Function<MessageHandler<T>, Client> clientBuilder) {
        synchronized (clientHealthEntries) {

            ClientHealthEntry<T> healthEntry = ClientHealthEntry.<T>builder()
                    .clientBuilder(clientBuilder)
                    .reconnectAttempts(0L)
                    .lastTimeSeen(0L)
                    .build();

            HealthCheckMessageHandler<K, T> healthCheckMessageHandler = HealthCheckMessageHandler.<K, T>builder()
                    .nodeId(nodeID)
                    .clientForNode(clientsForNodeId)
                    .messageFactory(echoMessageFactory)
                    .targetMessageHandler(targetMessageHandler)
                    .healthEntry(healthEntry)
                    .build();

            healthEntry.setCurrentClient(clientBuilder.apply(healthCheckMessageHandler));
        }
    }

    @Override
    public void close() throws Exception {
        // TODO shutdown threads

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
}
