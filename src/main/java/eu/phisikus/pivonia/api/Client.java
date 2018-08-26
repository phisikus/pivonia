package eu.phisikus.pivonia.api;

import io.vavr.control.Try;

public interface Client extends AutoCloseable {
    /**
     * Send message to connected client
     *
     * @param message message that will be sent to the connected client
     * @return if successful return itself, otherwise exception that occurred
     */
    Try<Client> send(Message message);


    /**
     * Connect to the client
     *
     * @param address address of the client
     * @param port    port of the client
     * @return client connected to given address or exception that occurred
     */
    Try<Client> connect(String address, int port);
}