package eu.phisikus.pivonia.logic;

import lombok.Value;

import java.util.function.Consumer;

@Value
public class MessageHandler<T> {
    private final Class<T> messageType;
    private final Consumer<T> messageHandler;

    MessageHandler(Class<T> messageType, Consumer<T> messageHandler) {
        this.messageType = messageType;
        this.messageHandler = messageHandler;
    }

    public static <T> MessageHandler<T> create(Class<T> messageType, Consumer<T> messageHandler) {
        return new MessageHandler<>(messageType, messageHandler);
    }
}
