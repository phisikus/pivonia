package eu.phisikus.pivonia.middleware;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.MessageHandler;
import eu.phisikus.pivonia.api.middleware.Middleware;
import eu.phisikus.pivonia.api.middleware.MiddlewareClient;
import io.vavr.collection.List;
import lombok.ToString;

import java.util.Optional;

@ToString
class MiddlewareClientImpl<T> implements MiddlewareClient<T> {

    private final Class<T> messageType;
    private final List<Middleware<T>> middlewares;
    private final int index;

    MiddlewareClientImpl(Class<T> messageType,
                         List<Middleware<T>> middlewares, int index) {
        this.messageType = messageType;
        this.middlewares = middlewares;
        this.index = index;
    }


    @Override
    public void sendMessage(T message) {
        List<Middleware<T>> layersAbove = middlewares
                .subSequence(0, index)
                .reverse();
        callOutgoingMessageHandlers(message, layersAbove);
    }

    private void callOutgoingMessageHandlers(T outgoingMessage, List<Middleware<T>> layersAbove) {
        var currentMessage = Optional.of(outgoingMessage);
        for (var layer : layersAbove) {
            currentMessage = layer.handleOutgoingMessage(currentMessage.get());
            if (!currentMessage.isPresent()) {
                break;
            }
        }
    }

    @Override
    public MessageHandler<T> getMessageHandler() {
        return new MessageHandler<>() {
            List<Middleware<T>> layersBelow = middlewares.subSequence(index);

            @Override
            public void handleMessage(T incomingMessage, Client client) {
                callIncomingMessageHandlers(incomingMessage, layersBelow);
            }

            @Override
            public Class<T> getMessageType() {
                return messageType;
            }
        };
    }

    private void callIncomingMessageHandlers(T incomingMessage, List<Middleware<T>> layersBelow) {
        var currentMessage = Optional.of(incomingMessage);
        for (var layer : layersBelow) {
            currentMessage = layer.handleIncomingMessage(currentMessage.get());
            if (!currentMessage.isPresent()) {
                break;
            }
        }
    }
}
