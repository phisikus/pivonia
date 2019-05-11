package eu.phisikus.pivonia.utils

import eu.phisikus.pivonia.logic.MessageHandlers
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class PivoniaSpec extends Specification {

    def "Should provide default connection manager"() {
        given: "application algorithm is defined"
        def messageHandlers = MessageHandlers.create()

        and: "node ID is defined"
        def nodeId = UUID.randomUUID().toString()

        when: "helper instance is created"
        def pivonia = Pivonia.builder()
                .nodeId(nodeId)
                .messageHandlers(messageHandlers)
                .build()

        then: "it can provide Connection Manager"
        def connectionManager = pivonia.getConnectionManager()
        connectionManager != null


        cleanup: "dispose of Connection Manager"
        connectionManager.dispose()
    }

    def "Should not build helper instance when field values are missing"() {
        when: "helper instance is created without required settings"
        Pivonia.builder().build()

        then: "exception should be thrown"
        thrown(IllegalArgumentException)
    }

    def "Should provide configured connection manager, clients and servers"() {
        given: "application algorithm is defined"
        def messageHandlers = MessageHandlers.create()

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
        def pivonia = Pivonia.builder()
                .nodeId(nodeId)
                .state(state)
                .timeoutDelay(timeoutDelay)
                .heartbeatDelay(heartbeatDelay)
                .maxConnectionRetryAttempts(maxConnectionRetryAttempts)
                .messageHandlers(messageHandlers)
                .encryptionKey(encryptionKey)
                .build()

        then: "it can provide Connection Manager"
        def connectionManager = pivonia.getConnectionManager()
        connectionManager != null

        and: "it can provide Client instances"
        pivonia.getClient() != null
        pivonia.getClientWithEncryption() != null

        and: "it can provide Server instances"
        pivonia.getServer() != null
        pivonia.getServerWithEncryption() != null

        and: "it can provide state"
        pivonia.getState() == state

        and: "return node ID"
        pivonia.getNodeId() == nodeId

        cleanup: "dispose of Connection Manager and encryption key"
        connectionManager.dispose()
        Files.delete(Path.of(keyFilename))
    }
}
