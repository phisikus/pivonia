package eu.phisikus.pivonia.logic;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.MessageWithClient;
import eu.phisikus.pivonia.api.Server;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

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

    public <T> MessageHandlers withHandler(MessageHandler<T> messageHandler) {
        var newList = new LinkedList<>(this.messageHandlers);
        newList.add(messageHandler);
        return new MessageHandlers(newList);
    }

    public MessageHandlers build() {
        return this;
    }

    public void registerHandlers(Client client) {
        registerHandlers(client::getMessages);
    }

    public void registerHandlers(Server server) {
        registerHandlers(server::getMessages);
    }

    private <T> void registerHandlers(Function<Class<T>, Observable<MessageWithClient<T>>> messageSource) {
        messageHandlers.forEach(handler -> {
            var messageType = handler.getMessageType();
            var messageHandler = handler.getMessageHandler();
            var subscription = messageSource
                    .apply(messageType)
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