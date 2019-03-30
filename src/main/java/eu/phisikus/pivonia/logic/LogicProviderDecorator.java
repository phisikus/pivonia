package eu.phisikus.pivonia.logic;

import eu.phisikus.pivonia.api.Receiver;

import javax.inject.Provider;

/**
 * This class decorates given provider with additional application logic passed in form of MessageHandlers
 *
 * @param <K> type of Receiver like Server or Client
 */
public class LogicProviderDecorator<K extends Receiver> implements Provider<K> {
    private final Provider<K> receiverProvider;
    private final MessageHandlers messageHandlers;

    public LogicProviderDecorator(Provider<K> receiverProvider, MessageHandlers messageHandlers) {
        this.receiverProvider = receiverProvider;
        this.messageHandlers = messageHandlers;
    }

    @Override
    public K get() {
        var newReceiver = receiverProvider.get();
        messageHandlers.registerHandlers(newReceiver);
        return newReceiver;
    }
}
