package eu.phisikus.pivonia.api;

import io.vavr.control.Try;

public interface Server extends Receiver, AutoCloseable {

    /**
     * Start listening on given port.
     *
     * @param port number of the port
     * @return instance of the Server that will handle the traffic
     */
    Try<Server> bind(int port);

    /**
     * Start listening on given port and address.
     *
     * @param address address that the server should listen on
     * @param port    number of the port
     * @return instance of the Server that will handle the traffic
     */
    Try<Server> bind(String address, int port);
}
