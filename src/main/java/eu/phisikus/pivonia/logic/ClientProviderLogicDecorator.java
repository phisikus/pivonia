package eu.phisikus.pivonia.logic;

import eu.phisikus.pivonia.api.Client;

import javax.inject.Provider;

/**
 * This class provides Client instance decorated with message handlers.
 * The purpose of this class is to act as a source of properly configured clients for other components.
 *
 */
public class ClientProviderLogicDecorator implements Provider<Client> {

    private final Provider<Client> clientProvider;
    private final MessageHandlers messageHandlers;

    public ClientProviderLogicDecorator(Provider<Client> clientProvider, MessageHandlers messageHandlers) {
        this.clientProvider = clientProvider;
        this.messageHandlers = messageHandlers;
    }

    @Override
    public Client get() {
        var newClient = clientProvider.get();
        messageHandlers.registerHandlers(newClient);
        return newClient;
    }
}
