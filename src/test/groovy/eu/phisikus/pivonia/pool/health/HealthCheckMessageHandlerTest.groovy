package eu.phisikus.pivonia.pool.health

import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.api.MessageHandler
import eu.phisikus.pivonia.api.TestMessage
import spock.lang.Specification

class HealthCheckMessageHandlerTest extends Specification {
    def message = new TestMessage(0L, "general", "Hello")

    def 'Should pass incoming application message to target handler'() {

        given: "target message handler is defined"
        def messageHandler = Mock(MessageHandler)
        def messageFactory = Mock(EchoMessageFactory)
        def client = Mock(Client)

        and: "the message is not a health check"
        1 * messageFactory.verifyMessage(message) >> Optional.empty()

        when: "HealthCheckMessageHandler is defined"
        def healthCheckMessageHandler = HealthCheckMessageHandler.builder()
                .messageFactory(messageFactory)
                .targetMessageHandler(messageHandler)
                .build()

        and: "internal message handler is called"
        healthCheckMessageHandler.handleMessage(message, client)

        then: "the message is passed to the target message handler"
        1 * messageHandler.handleMessage(message, client)
    }

    def "Should update health record when health message appears"() {

        given: "target message handler and other mocks are defined"
        def messageHandler = Mock(MessageHandler)
        def messageFactory = Mock(EchoMessageFactory)
        def healthEntry = Mock(ClientHealthEntry)
        def client = Mock(Client)
        def mappings = Mock(Map)
        def nodeKey = "ID"

        and: "the test message is a heartbeat"
        1 * messageFactory.verifyMessage(message) >> Optional.of(nodeKey)

        when: "HealthCheckMessageHandler is defined"
        def healthCheckMessageHandler = HealthCheckMessageHandler.builder()
                .messageFactory(messageFactory)
                .healthEntry(healthEntry)
                .clientForNode(mappings)
                .targetMessageHandler(messageHandler)
                .build()

        and: "internal message handler is called"
        healthCheckMessageHandler.handleMessage(message, client)

        then: "the message is not passed to the target handler but the client mappings are updated"
        0 * messageHandler.handleMessage(message, client)
        1 * mappings.put(nodeKey, client)
        1 * healthEntry.setCurrentClient(client)
        1 * healthEntry.setLastTimeSeen(_)

    }
}
