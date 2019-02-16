package eu.phisikus.pivonia.logic;

import eu.phisikus.pivonia.api.Server;

import javax.inject.Provider;

/**
 * This class provides Server instance decorated with message handlers.
 * The purpose of this class is to act as a source of properly configured servers for other components.
 */
public class ServerProviderLogicDecorator implements Provider<Server> {

    private final Provider<Server> serverProvider;
    private final MessageHandlers messageHandlers;

    public ServerProviderLogicDecorator(Provider<Server> serverProvider, MessageHandlers messageHandlers) {
        this.serverProvider = serverProvider;
        this.messageHandlers = messageHandlers;
    }

    @Override
    public Server get() {
        var newServer = serverProvider.get();
        messageHandlers.registerHandlers(newServer);
        return newServer;
    }
}
