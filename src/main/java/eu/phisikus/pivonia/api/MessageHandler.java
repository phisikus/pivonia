package eu.phisikus.pivonia.api;

public interface MessageHandler<T> {
    /**
     * Used to provide callback for handling incoming messages.
     *
     * @param incomingMessage new incoming message
     * @param client          connected client that you can use to respond
     */
    void handleMessage(T incomingMessage, Client client);


    /**
     * Returns message type supported by this handler
     *
     * @return message type
     */
    Class<T> getMessageType();
}
