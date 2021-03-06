package eu.phisikus.pivonia.it

import eu.phisikus.pivonia.api.EmptyEnvelope
import eu.phisikus.pivonia.logic.MessageHandler
import eu.phisikus.pivonia.logic.MessageHandlers
import eu.phisikus.pivonia.node.Node
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class NodeClientServerITSpec extends Specification {

    def pollingConditions = new PollingConditions(delay: 1, timeout: 10)

    def "Should get working client and server that communicate with each other"() {
        given: "there is a test message"
        def nodeId = UUID.randomUUID().toString()
        def message = new EmptyEnvelope<>(nodeId, nodeId)

        and: "defined algorithm that notifies about message coming in"
        def isMessageReceived = false
        def handler = { ctx, event -> isMessageReceived = event.getMessage().equals(message) && ctx != null }
        def messageHandlers = MessageHandlers.create()
                .withHandler(MessageHandler.create(EmptyEnvelope, handler))

        and: "framework is configured"
        def node = Node.builder()
                .id(nodeId)
                .messageHandlers(messageHandlers)
                .build()

        and: "server is created"
        def serverAddress = node.getNetworkAddressResolver()
                .getAddress()
                .get()
        def server = node.getServer()
                .bind(serverAddress.port)
                .get()

        and: "client is connected"
        def client = node.getClient()
                .connect(serverAddress.hostname, serverAddress.port)
                .get()

        when: "message is sent"
        client.send(message)

        then: "message is received"
        pollingConditions.eventually {
            assert isMessageReceived
        }

        cleanup: "free up resources"
        ignoringException {
            server.close()
            client.close()
            node.dispose()
        }
    }

    private static def ignoringException(Runnable runnable) {
        try {
            runnable.run()
        } finally {
            // do nothing
        }
    }
}
