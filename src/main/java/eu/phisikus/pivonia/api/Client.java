package eu.phisikus.pivonia.api;

import io.vavr.control.Try;

public interface Client extends Transmitter, Receiver {

    /**
     * Connect the client using provided address.
     *
     * @param address address of the client
     * @param port    port of the client
     * @return client connected to given address or exception that occurred
     */
    Try<Client> connect(String address, int port);
}
