package eu.phisikus.pivonia.api;

import io.vavr.control.Try;

public interface Transmitter extends AutoCloseable {
    /**
     * Send message to connected node.
     *
     * @param message message that will be sent to the connected node
     * @return if successful return itself, otherwise exception that occurred
     */
    <T> Try<? extends Transmitter> send(T message);
}
