package eu.phisikus.pivonia.logic


import eu.phisikus.pivonia.api.Server
import spock.lang.Specification

import javax.inject.Provider

class ServerProviderLogicDecoratorSpec extends Specification {
    def "Should provide server with configured message handlers"() {
        given: "MessageHandler is defined"
        def messageHandlers = Mock(MessageHandlers)

        and: "generic Server provider is defined"
        def serverProvider = Mock(Provider)
        def server = Mock(Server)

        and: "ServerProviderLogicDecorator is configured"
        def decorator = new ServerProviderLogicDecorator(serverProvider, messageHandlers)

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
