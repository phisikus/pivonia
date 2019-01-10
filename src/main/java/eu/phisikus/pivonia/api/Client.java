package eu.phisikus.pivonia.api;

import io.vavr.control.Try;

public interface Client extends AutoCloseable {
    /**
     * Send message to connected client.
     *
     * @param message message that will be sent to the connected client
     * @return if successful return itself, otherwise exception that occurred
     */
    <T> Try<Client> send(T message);


    /**
     * Connect the client using provided address.
     *
     * @param address        address of the client
     * @param port           port of the client
     * @return client connected to given address or exception that occurred
     */
    Try<Client> connect(String address, int port);


    /**
     * Register message handler for certain type of incoming message.
     * Only one handler should be registered per message type.
     *
     * @param messageHandler message handler called for incoming messages
     * @param <T> type of messages
     * @return client with registered handler
     */
    <T> Client addHandler(MessageHandler<T> messageHandler);
}
