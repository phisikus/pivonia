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

    /**
     * Create empty instance of MessageHandlers.
     *
     * @param <C> type of context
     * @return new MessageHandlers instance
     */
    public static <C> MessageHandlers<C> create() {
        return new MessageHandlers<>(null, new LinkedList<>(), new LinkedList<>());
    }

    /**
     * Add given MessageHandler to the aggregate.
     * Returned new MessageHandlers instance will contain all of the previously added handlers plus the given MessageHandler.
     *
     * @param messageHandler single MessageHandler that should be added to existing handlers
     * @param <T>            type of message
     * @return new MessageHandlers instance
     */
    public <T> MessageHandlers<C> withHandler(@NonNull MessageHandler<C, T> messageHandler) {
        var newHandlersList = new LinkedList<>(this.messageHandlers);
        var newSubscriptionList = new LinkedList<>(this.subscriptions);
        newHandlersList.add(messageHandler);
        return new MessageHandlers<>(context, newHandlersList, newSubscriptionList);
    }

    /**
     * Combine existing handlers with handlers from provided instance and create new MessageHandlers.
     *
     * @param messageHandlers handlers that should be added
     * @return new MessageHandlers instance with combined handlers inside
     */
    public MessageHandlers<C> withHandlers(@NonNull MessageHandlers<C> messageHandlers) {
        var newHandlersList = new LinkedList<>(this.messageHandlers);
        var newSubscriptionList = new LinkedList<>(this.subscriptions);
        newHandlersList.addAll(messageHandlers.messageHandlers);
        newSubscriptionList.addAll(messageHandlers.subscriptions);
        return new MessageHandlers<C>(context, newHandlersList, newSubscriptionList);
    }

    /**
     * Prepare MessageHandlers for usage by providing context object that will be passed with every handler call.
     *
     * @param context object that will be passed as context
     * @return new ready-to-use MessageHandlers instance
     */
    public MessageHandlers build(C context) {
        return new MessageHandlers<>(context, messageHandlers, subscriptions);
    }

    /**
     * Register message subscriptions.
     * For each message handler within this MessageHandlers instance, subscription will be made using provided Receiver.
     *
     * @param receiver instance of Receiver that should be visited
     */
    public void registerHandlers(@NonNull Receiver receiver) {
        messageHandlers.forEach(handler -> {
            var subscription = receiver
                    .getMessages(handler.getMessageType())
                    .subscribe(event -> handleMessageEvent((MessageWithTransmitter) event, handler));
            subscriptions.add(subscription);
        });
    }

    private void handleMessageEvent(MessageWithTransmitter messageEvent, MessageHandler handler) {
        var messageHandler = handler.getMessageHandler();
        messageHandler.accept(context, messageEvent);
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
