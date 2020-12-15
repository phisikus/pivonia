package eu.phisikus.pivonia.logic;

import eu.phisikus.pivonia.api.MessageWithTransmitter;
import lombok.NonNull;
import lombok.Value;

import java.util.function.BiConsumer;

/**
 * Represents message handler meant to be used for binding application logic with communication layer.
 * It consists of type definition and function that consumes message of that type.
 * Additionally you can access some context object that can provide additional features.
 *
 * @param <C> type of context object
 * @param <T> type of message
 */
@Value
public class MessageHandler<C, T> {
    private final Class<T> messageType;
    private final BiConsumer<C, MessageWithTransmitter<T>> messageHandler;

    /**
     * Create message handler for given type and message consuming function.
     *
     * @param messageType    class type of message handled by this MessageHandler
     * @param messageHandler message consuming function for that type
     * @param <C>            generic type of context object available for this MessageHandler
     * @param <T>            generic type of message handled by this MessageHandler
     * @return new and ready to use MessageHandler
     */
    public static <C, T> MessageHandler<C, T> create(@NonNull Class<T> messageType, @NonNull BiConsumer<C, MessageWithTransmitter<T>> messageHandler) {
        return new MessageHandler<>(messageType, messageHandler);
    }
}
