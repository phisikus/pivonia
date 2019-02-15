package eu.phisikus.pivonia.logic;

import eu.phisikus.pivonia.api.Client;
import io.reactivex.disposables.Disposable;

import java.util.LinkedList;
import java.util.List;

public class MessageHandlers implements Disposable {
    private final List<MessageHandler> messageHandlers;
    private final List<Disposable> subscriptions;

    MessageHandlers(List<MessageHandler> messageHandlers) {
        this.messageHandlers = messageHandlers;
        this.subscriptions = new LinkedList<>();
    }

    public MessageHandlers create() {
        return new MessageHandlers(new LinkedList<>());
    }

    public <T> MessageHandlers addHandler(MessageHandler<T> messageHandler) {
        var newList = new LinkedList<>(this.messageHandlers);
        newList.add(messageHandler);
        return new MessageHandlers(newList);
    }

    public void registerHandlers(Client client) {
        messageHandlers.forEach(messageHandler -> {
            var subscription = client
                    .getMessages(messageHandler.getMessageType())
                    .subscribe(message -> messageHandler.getMessageHandler().accept(message));
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

    //TODO add tests, add for server
}
