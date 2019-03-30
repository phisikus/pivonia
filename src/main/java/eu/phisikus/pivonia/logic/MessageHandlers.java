package eu.phisikus.pivonia.logic;

import eu.phisikus.pivonia.api.Receiver;
import io.reactivex.disposables.Disposable;
import lombok.NonNull;

import java.util.LinkedList;
import java.util.List;

public class MessageHandlers implements Disposable {
    private final List<MessageHandler> messageHandlers;
    private final List<Disposable> subscriptions;

    MessageHandlers(List<MessageHandler> messageHandlers) {
        this.messageHandlers = messageHandlers;
        this.subscriptions = new LinkedList<>();
    }

    public static MessageHandlers create() {
        return new MessageHandlers(new LinkedList<>());
    }

    public <T> MessageHandlers withHandler(@NonNull MessageHandler<T> messageHandler) {
        var newList = new LinkedList<>(this.messageHandlers);
        newList.add(messageHandler);
        return new MessageHandlers(newList);
    }

    public MessageHandlers build() {
        return this;
    }

    public void registerHandlers(@NonNull Receiver receiver) {
        messageHandlers.forEach(handler -> {
            var messageType = handler.getMessageType();
            var messageHandler = handler.getMessageHandler();
            var subscription = receiver.getMessages(messageType)
                    .subscribe(messageHandler::accept);
            subscriptions.add(subscription);
        });
    }

    @Override
    public void dispose() {
        subscriptions.forEach(Disposable::dispose);
    }

    @Override
    public boolean isDisposed() {
        return subscriptions.stream().allMatch(Disposable::isDisposed);
    }
}
