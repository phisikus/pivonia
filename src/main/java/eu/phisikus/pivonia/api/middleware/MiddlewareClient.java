package eu.phisikus.pivonia.api.middleware;

import eu.phisikus.pivonia.api.MessageHandler;

/**
 * Middleware Client represents a set of operations that can be performed by initialized Middleware.
 * The initialization process means that a Middleware layer is positioned with other optional layers inside of a "Cake".
 * That "cake" structure is wired together by providing each Middleware with a MiddlewareClient.
 * That client is responsible for passing data through the cake structure.
 */
public interface MiddlewareClient<T> {

    /**
     * It sends outgoing message to layer above with assumption that it will be processed and forwarded further.
     * In the end it could be sent out through network.
     *
     * @param message message that should be sent
     */
    void sendMessage(T message);

    /**
     * It returns an aggregated message handler chaining together message processors from layers going into the system.
     * If you would like to connect to outside source of messages this would be a good entrypoint.
     *
     * @return message handler that calls all message handlers below calling layer
     */
    MessageHandler<T> getMessageHandler();
}
