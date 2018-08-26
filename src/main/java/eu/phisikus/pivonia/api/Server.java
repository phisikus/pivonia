package eu.phisikus.pivonia.api;

import io.vavr.control.Try;

public interface Server extends AutoCloseable {

    /**
     * Start listening on given port and register a message handler for those messages
     *
     * @param port           number of the port
     * @param messageHandler message handler for incoming messages
     * @return instance of the Server  that will handle the traffic
     */
    Try<Server> bind(int port, MessageHandler messageHandler);

    /**
     * Start listening on given port & address and register a message handler for those messages
     *
     * @param address        address that the server should listen on
     * @param port           number of the port
     * @param messageHandler message handler for incoming messages
     * @return instance of the Server that will handle the traffic
     */
    Try<Server> bind(String address, int port, MessageHandler messageHandler);

}
