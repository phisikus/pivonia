package eu.phisikus.pivonia.node


import eu.phisikus.pivonia.api.Receiver
import eu.phisikus.pivonia.logic.MessageHandlers
import eu.phisikus.pivonia.node.Middleware
import eu.phisikus.pivonia.node.Node
import eu.phisikus.pivonia.utils.CryptoUtils
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class NodeSpec extends Specification {

    def "Should provide default connection manager"() {
        given: "application algorithm is defined"
        def messageHandlers = MessageHandlers.create()

        and: "node ID is defined"
        def nodeId = UUID.randomUUID().toString()

        when: "helper instance is created"
        def node = Node.builder()
                .id(nodeId)
                .messageHandlers(messageHandlers)
                .build()

        then: "it can provide Connection Manager"
        def connectionManager = node.getConnectionManager()
        connectionManager != null

        cleanup: "free up resources"
        node.dispose()
    }

    def "Should not build helper instance when field values are missing"() {
        when: "helper instance is created without required settings"
        Node.builder().build()

        then: "exception should be thrown"
        thrown(IllegalArgumentException)
    }

    def "Should provide configured connection manager, clients and servers"() {
        given: "application algorithm is defined"
        def messageHandlers = Mock(MessageHandlers)
        _ * messageHandlers.registerHandlers(_ as Receiver)

        and: "node ID is defined"
        def nodeId = UUID.randomUUID().toString()

        and: "timeout values are defined"
        def timeoutDelay = 1000
        def heartbeatDelay = 5000
        def maxConnectionRetryAttempts = 10

        and: "node state is defined"
        def state = 42L

        and: "encryption key is defined"
        def keyFilename = UUID.randomUUID().toString()
        CryptoUtils.buildKeyset(keyFilename)
        byte[] encryptionKey = CryptoUtils.getKeysetContent(keyFilename)


        when: "helper instance is created"
        def node = Node.builder()
                .id(nodeId)
                .state(state)
                .timeoutDelay(timeoutDelay)
                .heartbeatDelay(heartbeatDelay)
                .maxConnectionRetryAttempts(maxConnectionRetryAttempts)
                .messageHandlers(messageHandlers)
                .encryptionKey(encryptionKey)
                .build()

        then: "it can provide Connection Manager"
        def connectionManager = node.getConnectionManager()
        connectionManager != null

        and: "message handlers have context set up"
        1 * messageHandlers.build(_ as Node) >> messageHandlers

        and: "it can provide Client instances"
        node.getClient() != null
        node.getClientWithEncryption() != null

        and: "it can provide Server instances"
        node.getServer() != null
        node.getServerWithEncryption() != null

        and: "it can provide state"
        node.getState() == state

        and: "return node ID"
        node.getId() == nodeId

        cleanup: "free up the resources and delete encryption key"
        node.dispose()
        Files.delete(Path.of(keyFilename))
    }

    def "Should dispose of all created resources"() {
        given: "application algorithm is defined"
        def messageHandlers = MessageHandlers.create()

        and: "node ID is defined"
        def nodeId = UUID.randomUUID().toString()

        and: "test middleware is defined"
        def middleware = Mock(Middleware)
        def middlewareMessageHandlers = MessageHandlers.create()
        1 * middleware.getMessageHandlers() >> middlewareMessageHandlers
        1 * middleware.init(_)
        1 * middleware.dispose()
        1 * middleware.isDisposed() >> true

        when: "Node instance is created"
        def node = Node.builder()
                .id(nodeId)
                .middleware(middleware)
                .messageHandlers(messageHandlers)
                .build()
        def connectionManager = node.getConnectionManager()

        and: "disposal function is called"
        node.dispose()

        then: "middleware message handlers are disposed of"
        middlewareMessageHandlers.isDisposed()

        and: "application algorithms message handlers are disposed of"
        messageHandlers.isDisposed()

        and: "networking components are disposed of as well"
        connectionManager.isDisposed()

        and: "Node instance itself identifies as disposed"
        node.isDisposed()
    }
}
