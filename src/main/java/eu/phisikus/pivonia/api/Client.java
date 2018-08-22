package eu.phisikus.pivonia.api;

import io.vavr.control.Try;

public interface Client {
    /**
     * Send message to client
     *
     * @param message message that will be sent to the connected client
     * @return either success with number of bytes sent or exception that occured
     */
    Try<Integer> send(Message message);
}
