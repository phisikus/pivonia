package eu.phisikus.pivonia.api;

@FunctionalInterface
public interface MessageHandler {
    /**
     * Used to provide callback for handling incoming messages.
     *
     * @param incomingMessage new incoming message
     * @param client          connected client that you can use to respond
     */
    void handleMessage(Message incomingMessage, Client client);
}
