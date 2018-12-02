package eu.phisikus.pivonia.middleware.layer.pool;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.MessageHandler;
import eu.phisikus.pivonia.api.Server;
import eu.phisikus.pivonia.api.pool.ClientPool;
import eu.phisikus.pivonia.api.pool.HasSenderId;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class BasicClientPool<K, T extends HasSenderId<K>> implements ClientPool<K, T> {

    private ConcurrentMap<K, Client> clients = new ConcurrentHashMap<>();
    private MessageHandler<T> clientHandler;
    private MessageHandler<T> serverHandler;

    public BasicClientPool(MessageHandler<T> clientHandler, MessageHandler<T> serverHandler) {
        this.clientHandler = clientHandler;
        this.serverHandler = serverHandler;
    }

    @Override
    public Optional<Client> get(K id) {
        return Optional.ofNullable(clients.get(id));
    }

    @Override
    public boolean exists(K id) {
        return clients.containsKey(id);
    }

    @Override
    public void addUsingBuilder(Function<MessageHandler<T>, Client> clientBuilder) {
        clientBuilder.apply(buildMessageHandler(clientHandler));
    }

    @Override
    public void addSourceUsingBuilder(Function<MessageHandler<T>, Server> serverBuilder) {
        serverBuilder.apply(buildMessageHandler(serverHandler));
    }

    @Override
    public void set(K id, Client client) {
        clients.put(id, client);
    }

    @Override
    public void remove(Client client) {
        clients.entrySet()
                .stream()
                .filter(entry -> client.equals(entry.getValue()))
                .forEach(entry -> clients.remove(entry.getKey(), entry.getValue()));
    }

    private MessageHandler<T> buildMessageHandler(MessageHandler<T> commonMessageHandler) {
        return new MessageHandler<>() {
            @Override
            public void handleMessage(T incomingMessage, Client client) {
                clients.put(incomingMessage.getSenderId(), client);
                commonMessageHandler.handleMessage(incomingMessage, client);
            }

            @Override
            public Class<T> getMessageType() {
                return commonMessageHandler.getMessageType();
            }
        };
    }

    @Override
    public void close() throws Exception {
        for (Client client : clients.values()) {
            client.close();
        }
    }


}
