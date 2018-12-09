package eu.phisikus.pivonia.middleware.layer.pool;

import eu.phisikus.pivonia.api.MessageHandler;
import eu.phisikus.pivonia.api.middleware.Middleware;
import eu.phisikus.pivonia.api.middleware.MiddlewareClient;
import eu.phisikus.pivonia.api.pool.ClientPool;
import eu.phisikus.pivonia.api.pool.Envelope;
import eu.phisikus.pivonia.api.pool.MessageWithClient;
import eu.phisikus.pivonia.middleware.MissingMiddlewareException;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ClientPoolLayer<K, T extends Envelope<K>> implements Middleware<T> {

    private final ClientPool<K, T> clientPool;
    private final List<Disposable> disposables = new LinkedList<>();
    private MiddlewareClient<T> middlewareClient;

    public ClientPoolLayer(ClientPool<K, T> clientPool) {
        this.clientPool = clientPool;
    }


    @Override
    public void initialize(MiddlewareClient<T> middlewareClient) throws MissingMiddlewareException {
        this.middlewareClient = middlewareClient;
        var messageHandler = middlewareClient.getMessageHandler();
        bindSubscription(messageHandler, clientPool.getClientMessages());
        bindSubscription(messageHandler, clientPool.getServerMessages());
    }

    private void bindSubscription(MessageHandler<T> messageHandler, Observable<MessageWithClient<T>> messages) {
        var subscription = messages.subscribe(
                event -> messageHandler.handleMessage(event.getMessage(), event.getClient())
        );
        disposables.add(subscription);
    }

    @Override
    public Optional<T> handleIncomingMessage(T message) {
        return Optional.of(message);
    }

    @Override
    public Optional<T> handleOutgoingMessage(T message) {
        clientPool.get(message.getRecipientId())
                .ifPresent(client -> client.send(message));
        return Optional.of(message);
    }

    @Override
    public void close() throws Exception {
        disposables.forEach(Disposable::dispose);
        clientPool.close();
    }
}
