package eu.phisikus.pivonia.logic

import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.api.Server
import spock.lang.Specification

import javax.inject.Provider

class LogicProviderDecoratorSpec extends Specification {
    def "Should provide client with configured message handlers"() {
        given: "MessageHandler is defined"
        def messageHandlers = Mock(MessageHandlers)

        and: "generic Client provider is defined"
        def clientProvider = Mock(Provider)
        def client = Mock(Client)

        and: "LogicProviderDecorator is configured"
        def decorator = new LogicProviderDecorator(clientProvider, messageHandlers)

        when: "decorator is called to provide Client"
        def actualClient = decorator.get()

        then: "client is returned"
        actualClient == client

        and: "provider was used to produce it"
        1 * clientProvider.get() >> client

        and: "message handlers were registered"
        1 * messageHandlers.registerHandlers(client)
    }

    def "Should provide server with configured message handlers"() {
        given: "MessageHandler is defined"
        def messageHandlers = Mock(MessageHandlers)

        and: "generic Server provider is defined"
        def serverProvider = Mock(Provider)
        def server = Mock(Server)

        and: "LogicProviderDecorator is configured"
        def decorator = new LogicProviderDecorator(serverProvider, messageHandlers)

        when: "decorator is called to provide Server"
        def actualServer = decorator.get()

        then: "server is returned"
        actualServer == server

        and: "provider was used to produce it"
        1 * serverProvider.get() >> server

        and: "message handlers were registered"
        1 * messageHandlers.registerHandlers(server)
    }
}
