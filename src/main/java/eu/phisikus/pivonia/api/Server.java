package eu.phisikus.pivonia.api;

import io.vavr.control.Try;

public interface Server extends AutoCloseable {

    /**
     * Start listening on given port
     *
     * @param port           number of the port
     * @return instance of the Server  that will handle the traffic
     */
    Try<Server> bind(int port);

    /**
     * Start listening on given port & address
     *
     * @param address        address that the server should listen on
     * @param port           number of the port
     * @return instance of the Server that will handle the traffic
     */
    Try<Server> bind(String address, int port);

    /**
     * Register message handler for certain type of incoming message.
     * Only one handler should be registered per message type.
     *
     * @param messageHandler message handler called for incoming messages
     * @param <T> type of messages
     * @return server with registered handler
     */
    <T> Server addHandler(MessageHandler<T> messageHandler);

}
