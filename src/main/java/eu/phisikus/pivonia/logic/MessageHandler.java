package eu.phisikus.pivonia.logic;

import lombok.NonNull;
import lombok.Value;

import java.util.function.Consumer;

/**
 * Represents message handler meant to be used for binding application logic with communication layer.
 * It consists of type definition and function that consumes message of that type.
 *
 * @param <T> type of message
 */
@Value
public class MessageHandler<T> {
    private final Class<T> messageType;
    private final Consumer<T> messageHandler;

    MessageHandler(Class<T> messageType, Consumer<T> messageHandler) {
        this.messageType = messageType;
        this.messageHandler = messageHandler;
    }

    /**
     * Create message handler for given type and message consuming function.
     *
     * @param messageType    class type of message handled by this MessageHandler
     * @param messageHandler message consuming function for that type
     * @param <T>            generic type of message handled by this MessageHandler
     * @return new and ready to use MessageHandler
     */
    public static <T> MessageHandler<T> create(@NonNull Class<T> messageType, @NonNull Consumer<T> messageHandler) {
        return new MessageHandler<>(messageType, messageHandler);
    }
}
