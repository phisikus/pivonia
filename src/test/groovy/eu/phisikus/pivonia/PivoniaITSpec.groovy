package eu.phisikus.pivonia

import eu.phisikus.pivonia.api.EmptyEnvelope
import eu.phisikus.pivonia.logic.MessageHandler
import eu.phisikus.pivonia.logic.MessageHandlers
import eu.phisikus.pivonia.test.ServerTestUtils
import eu.phisikus.pivonia.utils.Pivonia
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class PivoniaITSpec extends Specification {

    def pollingConditions = new PollingConditions(delay: 1, timeout: 10)

    def "Should get working client and server that communicate with each other"() {
        given: "there is a test message"
        def nodeId = UUID.randomUUID().toString()
        def message = new EmptyEnvelope<>(nodeId, nodeId)

        and: "defined algorithm that notifies about message coming in"
        def isMessageReceived = false
        def handler = { it -> isMessageReceived = true }
        def messageHandlers = MessageHandlers.create()
                .withHandler(MessageHandler.create(EmptyEnvelope, handler))
                .build()

        and: "framework is configured"
        def pivonia = Pivonia.builder()
                .nodeId(nodeId)
                .messageHandlers(messageHandlers)
                .build()

        and: "server is created"
        def port = ServerTestUtils.getRandomPort()
        def server = pivonia.getServer()
                .bind(port)
                .get()

        and: "client is connected"
        def client = pivonia.getClient()
                .connect("localhost", port)
                .get()

        when: "message is sent"
        client.send(message)

        then: "message is received"
        pollingConditions.eventually {
            isMessageReceived
        }

        cleanup:
        server.close()
        client.close()
    }
}
