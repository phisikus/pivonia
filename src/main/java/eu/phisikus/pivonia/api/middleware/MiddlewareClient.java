package eu.phisikus.pivonia.api.middleware;

import eu.phisikus.pivonia.api.MessageHandler;

/**
 * It represents a set of operations that a Middleware can perform inside the Cake.
 */
public interface MiddlewareClient<T> {

    /**
     * It sends outgoing message to parent layer with assumption that it will be processed and forwarded further.
     * In the end it should be sent out through network.
     *
     * @param message message that should be sent
     */
    void sendMessage(T message);

    /**
     * It returns an aggregated message handler chaining together message processors from layers going into the system.
     *
     * @return message handler that calls all message handlers below calling layer
     */
    MessageHandler<T> getMessageHandler();
}
