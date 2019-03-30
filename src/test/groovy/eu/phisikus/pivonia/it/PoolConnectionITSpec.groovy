package eu.phisikus.pivonia.it

import eu.phisikus.pivonia.api.EmptyEnvelope
import eu.phisikus.pivonia.logic.MessageHandler
import eu.phisikus.pivonia.logic.MessageHandlers
import eu.phisikus.pivonia.pool.heartbeat.HeartbeatMessage
import eu.phisikus.pivonia.pool.heartbeat.HeartbeatServerVisitor
import eu.phisikus.pivonia.test.ServerTestUtils
import eu.phisikus.pivonia.utils.Pivonia
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class PoolConnectionITSpec extends Specification {

    def pollingConditions = new PollingConditions(delay: 1, timeout: 10)

    def "Should perform communication using connection manager"() {
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
                .maxConnectionRetryAttempts(3)
                .heartbeatDelay(100)
                .timeoutDelay(10000)
                .messageHandlers(messageHandlers)
                .build()

        and: "connection manager is created"
        def connectionManager = pivonia.getConnectionManager()

        when: "server is created"
        def port = ServerTestUtils.getRandomPort()
        def server = pivonia.getServer()
                .bind(port)
                .get()
        HeartbeatServerVisitor.registerHeartbeatListener(nodeId, server)

        and: "address is added to the pool"
        def addressPool = connectionManager.getAddressPool()
        addressPool.add("localhost", port)

        and: "message is sent"
        Thread.sleep(5000) // TODO make awaitabiliy
        connectionManager.getClientPool()
                .get(nodeId)
                .get()
                .send(message)

        then: "message is received"
        pollingConditions.eventually {
            isMessageReceived
        }

        cleanup:
        connectionManager.dispose()
        server.close()
    }
}
