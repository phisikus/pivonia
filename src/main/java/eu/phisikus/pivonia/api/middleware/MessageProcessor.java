package eu.phisikus.pivonia.api.middleware;

import java.util.Optional;

/**
 * It represents a message handler that could be chained together with other ones like it.
 * The message is digested and returned in some other form or not returned at all.
 * The idea is that some messages should not be passed further while other can in a changed form.
 *
 * @param <T> type of message
 */
@FunctionalInterface
public interface MessageProcessor<T> {
    /**
     * Procedure called when a message comes into the system.
     *
     * @param incomingMessage message instance
     * @return optional instance of the message for further processing.
     */
    Optional<T> processMessage(T incomingMessage);
}
