package eu.phisikus.pivonia.it

import eu.phisikus.pivonia.api.EmptyEnvelope
import eu.phisikus.pivonia.logic.MessageHandler
import eu.phisikus.pivonia.logic.MessageHandlers
import eu.phisikus.pivonia.node.Node
import eu.phisikus.pivonia.pool.heartbeat.HeartbeatServerVisitor
import eu.phisikus.pivonia.pool.transmitter.events.AssignmentEvent
import eu.phisikus.pivonia.test.ServerTestUtils
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class NodePoolConnectionITSpec extends Specification {

    def pollingConditions = new PollingConditions(delay: 1, timeout: 10)

    def "Should perform communication using connection manager"() {
        given: "there is a test message"
        def nodeId = UUID.randomUUID().toString()
        def message = new EmptyEnvelope<>(nodeId, nodeId)

        and: "defined algorithm that notifies about message coming in"
        def isMessageReceived = false
        def handler = { ctx, event -> isMessageReceived = (event.getMessage() == message) && ctx != null }
        def messageHandlers = MessageHandlers.create()
                .withHandler(MessageHandler.create(EmptyEnvelope, handler))

        and: "framework is configured"
        def node = Node.builder()
                .id(nodeId)
                .maxConnectionRetryAttempts(3)
                .heartbeatDelay(100)
                .timeoutDelay(10000)
                .messageHandlers(messageHandlers)
                .build()

        and: "connection manager is created"
        def connectionManager = node.getConnectionManager()

        and: "server is created"
        def serverAddress = node.getNetworkAddressResolver()
                .getAddress()
                .get()
        def server = node.getServer()
                .bind(serverAddress.port)
                .get()
        HeartbeatServerVisitor.registerHeartbeatListener(nodeId, server)

        and: "message is to be sent when transmitter pool associates it with node"
        connectionManager.getTransmitterPool()
                .getChanges()
                .filter({ it instanceof AssignmentEvent })
                .map({ (AssignmentEvent) it })
                .filter({ it.id == nodeId })
                .forEach({ it.transmitter.send(message) })

        when: "address is added to the pool"
        def addressPool = connectionManager.getAddressPool()
        addressPool.add(serverAddress.hostname, serverAddress.port)

        then: "message is sent and received by the server"
        pollingConditions.eventually {
            assert isMessageReceived
        }

        cleanup: "free up resources"
        node.dispose()
    }
}
