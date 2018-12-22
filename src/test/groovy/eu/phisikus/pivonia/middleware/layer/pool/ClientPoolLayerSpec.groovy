package eu.phisikus.pivonia.middleware.layer.pool

import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.api.MessageHandler
import eu.phisikus.pivonia.api.middleware.MiddlewareClient
import eu.phisikus.pivonia.api.pool.ClientPool
import eu.phisikus.pivonia.middleware.layer.pool.test.FakeMessage
import io.reactivex.Observable
import spock.lang.Specification
import spock.lang.Subject

class ClientPoolLayerSpec extends Specification {

    def clientPool = Mock(ClientPool)

    @Subject
    def clientPoolLayer = new ClientPoolLayer(clientPool)

    def "Should initialize client pool layer properly"() {
        given: "Middleware Client is defined"
        def middlewareClient = Mock(MiddlewareClient)
        def clientMessages = Mock(Observable)
        def serverMessages = Mock(Observable)

        when: "Layer is initialized"
        clientPoolLayer.initialize(middlewareClient)

        then: "Message handlers are connected"
        1 * clientPool.getClientMessages() >> clientMessages
        1 * clientPool.getServerMessages() >> serverMessages
        1 * middlewareClient.getMessageHandler() >> Mock(MessageHandler)
    }

    def "Should handle outgoing message"() {
        given: "Message is defined"
        def senderId = UUID.randomUUID()
        def recipientId = UUID.randomUUID()
        def message = new FakeMessage(senderId, recipientId)
        def client = Mock(Client)

        when: "Outgoing message handler is called"
        clientPoolLayer.handleOutgoingMessage(message)

        then: "Message is sent through client pool"
        1 * clientPool.get(recipientId) >> Optional.of(client)
        1 * client.send(message)
    }

    def "Should just pass incoming message"() {
        given: "Message is defined"
        def message = Mock(FakeMessage)

        expect: "Message to be passed through incoming message handler"
        clientPoolLayer.handleIncomingMessage(message) == Optional.of(message)
    }
}
