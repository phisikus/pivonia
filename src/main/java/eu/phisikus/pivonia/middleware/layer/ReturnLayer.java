package eu.phisikus.pivonia.middleware.layer;

import eu.phisikus.pivonia.api.middleware.Middleware;
import eu.phisikus.pivonia.api.middleware.MiddlewareClient;
import eu.phisikus.pivonia.middleware.MissingMiddlewareException;

import java.util.Optional;

/**
 * This layer sends back incoming messages to outgoing stream and can be used as a last middleware layer
 *
 * @param <T> type of message
 */
public class ReturnLayer<T> implements Middleware<T> {

    private MiddlewareClient<T> client;

    @Override
    public void initialize(MiddlewareClient<T> middlewareClient) throws MissingMiddlewareException {
        this.client = middlewareClient;
    }

    @Override
    public Optional<T> handleIncomingMessage(T message) {
        client.sendMessage(message);
        return Optional.empty();
    }

    @Override
    public Optional<T> handleOutgoingMessage(T message) {
        return Optional.of(message);
    }

    @Override
    public void close() throws Exception {
        // nothing to do here
    }
}
