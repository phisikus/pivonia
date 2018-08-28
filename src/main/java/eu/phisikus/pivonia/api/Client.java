package eu.phisikus.pivonia.api;

import io.vavr.control.Try;

public interface Client extends AutoCloseable {
    /**
     * Send message to connected client
     *
     * @param message message that will be sent to the connected client
     * @return if successful return itself, otherwise exception that occurred
     */
    <T> Try<Client> send(T message);


    /**
     * Connect to the client
     *
     * @param address        address of the client
     * @param port           port of the client
     * @param messageHandler handler that will be notified when server responds with a message
     * @return client connected to given address or exception that occurred
     */
    Try<Client> connect(String address, int port, MessageHandler messageHandler);
}
