package eu.phisikus.pivonia.middleware.layer.pool;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.MessageHandler;
import eu.phisikus.pivonia.api.Server;
import eu.phisikus.pivonia.api.pool.ClientPool;
import eu.phisikus.pivonia.api.pool.Envelope;
import eu.phisikus.pivonia.api.pool.MessageWithClient;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import io.vavr.control.Try;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

class ClientPoolImpl<K, T extends Envelope<K>> implements ClientPool<K, T> {

    private final ConcurrentMap<K, Client> clients = new ConcurrentHashMap<>();
    private final Subject<MessageWithClient<T>> clientMessages = PublishSubject.create();
    private final Subject<MessageWithClient<T>> serverMessages = PublishSubject.create();
    private final Class<T> messageType;

    public ClientPoolImpl(Class<T> messageType) {
        this.messageType = messageType;
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
    public Try<Client> addUsingBuilder(Function<MessageHandler<T>, Try<Client>> clientBuilder) {
        return clientBuilder.apply(buildMessageHandler(clientMessages));
    }

    @Override
    public Try<Server> addSourceUsingBuilder(Function<MessageHandler<T>, Try<Server>> serverBuilder) {
        return serverBuilder.apply(buildMessageHandler(serverMessages));
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

    @Override
    public Observable<MessageWithClient<T>> getClientMessages() {
        return clientMessages;
    }

    @Override
    public Observable<MessageWithClient<T>> getServerMessages() {
        return serverMessages;
    }

    private MessageHandler<T> buildMessageHandler(Subject<MessageWithClient<T>> messageStream) {
        return new MessageHandler<>() {
            @Override
            public void handleMessage(T incomingMessage, Client client) {
                clients.put(incomingMessage.getSenderId(), client);
                messageStream.onNext(new MessageWithClient<>(incomingMessage, client));
            }

            @Override
            public Class<T> getMessageType() {
                return messageType;
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
