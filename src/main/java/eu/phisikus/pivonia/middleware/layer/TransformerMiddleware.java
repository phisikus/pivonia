package eu.phisikus.pivonia.middleware.layer;

import eu.phisikus.pivonia.api.middleware.Middleware;
import eu.phisikus.pivonia.api.middleware.MiddlewareClient;
import eu.phisikus.pivonia.middleware.MissingMiddlewareException;

import java.util.Optional;
import java.util.function.Function;

/**
 * This middleware should be used whenever some type of transformation has to be performed on the message.
 *
 * @param <T> type of message used in the system
 */
public class TransformerMiddleware<T> implements Middleware<T> {

    private final Function<T, T> incomingConverter;
    private final Function<T, T> outgoingConverter;

    /**
     * Creates transforming middleware by setting up transforming functions for the in and outgoing messages.
     *
     * @param incomingConverter function that will be applied to incoming message
     * @param outgoingConverter function that will be applied to outgoing message
     */
    public TransformerMiddleware(Function<T, T> incomingConverter, Function<T, T> outgoingConverter) {
        this.incomingConverter = incomingConverter;
        this.outgoingConverter = outgoingConverter;
    }

    @Override
    public void initialize(MiddlewareClient<T> middlewareClient) throws MissingMiddlewareException {
        // Nothing to do here
    }

    @Override
    public Optional<T> handleIncomingMessage(T message) {
        T convertedMessage = incomingConverter.apply(message);
        return Optional.of(convertedMessage);
    }

    @Override
    public Optional<T> handleOutgoingMessage(T message) {
        T convertedMessage = outgoingConverter.apply(message);
        return Optional.of(convertedMessage);
    }

    @Override
    public void close() throws Exception {
        // Nothing to do here
    }
}
