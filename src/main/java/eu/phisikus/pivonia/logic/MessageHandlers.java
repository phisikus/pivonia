package eu.phisikus.pivonia.logic;

import eu.phisikus.pivonia.api.MessageWithTransmitter;
import eu.phisikus.pivonia.api.Receiver;
import io.reactivex.disposables.Disposable;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.LinkedList;
import java.util.List;

/**
 * Aggregates multiple message handler instances that can be used to register functionality in any Receiver.
 * You can build this composite to encapsulate algorithm and later fill in some context object.
 * That context object is available to message handler instances in runtime.
 *
 * @param <C> type of context
 */
@AllArgsConstructor
public class MessageHandlers<C> implements Disposable {

    private final C context;
    private final List<MessageHandler> messageHandlers;
    private final List<Disposable> subscriptions;

    public static <C> MessageHandlers<C> create() {
        return new MessageHandlers<>(null, new LinkedList<>(), new LinkedList<>());
    }

    public <T> MessageHandlers<C> withHandler(@NonNull MessageHandler<C, T> messageHandler) {
        var newHandlersList = new LinkedList<>(this.messageHandlers);
        var newSubscriptionList = new LinkedList<>(this.subscriptions);
        newHandlersList.add(messageHandler);
        return new MessageHandlers<>(context, newHandlersList, newSubscriptionList);
    }

    public MessageHandlers build(C context) {
        return new MessageHandlers<>(context, messageHandlers, subscriptions);
    }

    public void registerHandlers(@NonNull Receiver receiver) {
        messageHandlers.forEach(handler -> {
            var subscription = receiver
                    .getMessages(handler.getMessageType())
                    .subscribe(event -> handleMessageEvent((MessageWithTransmitter) event, handler));
            subscriptions.add(subscription);
        });
    }

    private void handleMessageEvent(MessageWithTransmitter messageEvent, MessageHandler handler) {
        var message = messageEvent.getMessage();
        var messageHandler = handler.getMessageHandler();
        messageHandler.accept(context, message);
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
