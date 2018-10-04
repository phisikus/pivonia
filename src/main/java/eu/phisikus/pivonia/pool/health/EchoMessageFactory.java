package eu.phisikus.pivonia.pool.health;

import io.vavr.control.Try;

import java.util.Optional;

/**
 * This interface represents a factory of "echo" messages of type T.
 * Those messages should contain a field of type K that represents sender ID.
 * This type of message will be used for checking Client's availability and not be processed at application level.
 *
 * @param <K> type of ID used for node identification
 * @param <T> type of message required by Client/Server
 */
public interface EchoMessageFactory<K, T> {

    /**
     * Produces a new "echo" message.
     *
     * @param id identifier of the sending node that will be inserted into the message
     * @return new echo message
     */
    T getMessage(K id);

    /**
     * Verifies if provided message is an "echo" message or just a regular application message.
     *
     * @param message input message
     * @return sender node ID if provided message is echo or empty if a regular message.
     */
    Optional<K> verifyMessage(T message);
}
