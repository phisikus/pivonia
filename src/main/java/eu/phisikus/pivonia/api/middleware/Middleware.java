package eu.phisikus.pivonia.api.middleware;

import eu.phisikus.pivonia.middleware.MissingMiddlewareException;

import java.util.Optional;

/**
 * Middleware represents a layer of processing.
 * Messages coming into the system are passed through one handler, outgoing through another.
 * Middleware can produce its own messages and pass them out through parent layer using provided MiddlewareClient.
 *
 * @param <T> type of message used for data transfer
 */
public interface Middleware<T> extends AutoCloseable {

    /**
     * In this method additional resources should be created for middleware to function.
     * An instance of MiddlewareClient will also be provided for further usage.
     *
     * @param middlewareClient reference to management layer that can be used to push outgoing messages
     * @throws MissingMiddlewareException exception thrown if some required layer is missing
     */
    void initialize(MiddlewareClient<T> middlewareClient) throws MissingMiddlewareException;

    /**
     * Message coming into the system will be processed by this handler.
     * The layer can do one of three things:
     *  - return a new message with changed contents
     *  - return the message unchanged (possibly produce side-effects)
     *  - return empty value which stops the message from being processed further
     *
     * @param message input message
     * @return output message or empty value
     */
    Optional<T> handleIncomingMessage(T message);

    /**
     * Message coming out of the system will be processed by this handler.
     * The layer can do one of three things:
     *  - return a new message with changed contents
     *  - return the message unchanged (possibly produce side-effects)
     *  - return empty value which stops the message from being processed further
     *
     * @param message input message
     * @return output message or empty value
     */
    Optional<T> handleOutgoingMessage(T message);

}
