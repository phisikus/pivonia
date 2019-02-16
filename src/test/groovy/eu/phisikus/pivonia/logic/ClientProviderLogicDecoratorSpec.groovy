package eu.phisikus.pivonia.logic


import eu.phisikus.pivonia.api.Client
import spock.lang.Specification

import javax.inject.Provider

class ClientProviderLogicDecoratorSpec extends Specification {
    def "Should provide client with configured message handlers"() {
        given: "MessageHandler is defined"
        def messageHandlers = Mock(MessageHandlers)

        and: "generic Client provider is defined"
        def clientProvider = Mock(Provider)
        def client = Mock(Client)

        and: "ClientProviderLogicDecorator is configured"
        def decorator = new ClientProviderLogicDecorator(clientProvider, messageHandlers)

        when: "decorator is called to provide Client"
        def actualClient = decorator.get()

        then: "client is returned"
        actualClient == client

        and: "provider was used to produce it"
        1 * clientProvider.get() >> client

        and: "message handlers were registered"
        1 * messageHandlers.registerHandlers(client)
    }
}
