package eu.phisikus.pivonia.pool.health;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.MessageHandler;
import eu.phisikus.pivonia.pool.ClientPool;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ClientPoolWithHealthCheck<K, T> implements ClientPool<K> {
    private final K nodeID;
    private final EchoMessageFactory<K, T> echoMessageFactory;
    private final MessageHandler<T> messageHandler;
    private final Long timeRate;

    private final Map<K, Client> clientsForNodeId = new ConcurrentHashMap<>();
    private final List<ClientHealthEntry<T>> clientHealthEntries = new LinkedList<>();


    public ClientPoolWithHealthCheck(K nodeID, Long timeRate, EchoMessageFactory<K, T> echoMessageFactory, MessageHandler<T> messageHandler) {
        this.nodeID = nodeID;
        this.echoMessageFactory = echoMessageFactory;
        this.messageHandler = messageHandler;
        this.timeRate = timeRate;
    }


    @Override
    public Optional<Client> getClient(K id) {
        return Optional.empty();
    }

    @Override
    public boolean isAvailable(K id) {
        return false;
    }

    void addClient(Function<MessageHandler<T>, Client> clientBuilder) {
        synchronized (clientHealthEntries) {
            clientHealthEntries.add(ClientHealthEntry.<T>builder()
                    .clientBuilder(clientBuilder)
                    .reconnectAttempts(0L)
                    .lastTimeSeen(0L)
                    //.currentClient(clientBuilder.apply()) // TODO add
                    .build());
        }
    }

    @Override
    public void close() throws Exception {

    }
}
