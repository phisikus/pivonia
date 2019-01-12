package eu.phisikus.pivonia.pool

import eu.phisikus.pivonia.test.ServerTestUtils
import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.api.EmptyEnvelope
import eu.phisikus.pivonia.api.MessageHandler
import eu.phisikus.pivonia.api.pool.MessageWithClient
import eu.phisikus.pivonia.converter.DaggerConverterComponent
import eu.phisikus.pivonia.crypto.CryptoModule
import eu.phisikus.pivonia.crypto.DaggerCryptoComponent
import eu.phisikus.pivonia.tcp.TCPClient
import eu.phisikus.pivonia.tcp.TCPServer
import io.reactivex.observers.TestObserver
import spock.lang.Specification
import spock.lang.Subject
import spock.util.concurrent.PollingConditions

class ClientPoolImplITSpec extends Specification {

    @Subject
    def clientPool = new ClientPoolImpl(EmptyEnvelope)

    def encryptionKey = UUID.randomUUID().toString().getBytes()
    def cryptoModule = new CryptoModule(encryptionKey)
    def cryptoComponent = DaggerCryptoComponent.builder()
            .cryptoModule(cryptoModule)
            .build()
    def converterComponent = DaggerConverterComponent.builder()
            .cryptoComponent(cryptoComponent)
            .build()
    def converter = converterComponent.getBSONConverter()

    def pollingConditions = new PollingConditions(timeout: 5)

    def PORT = ServerTestUtils.getRandomPort()

    def "Should register client when registered server accepts message from new node"() {
        given: "server is added to the pool"
        def server = clientPool.addSourceUsingBuilder({
            handler -> new TCPServer(converter).addHandler(handler).bind(PORT)
        }).get()

        and: "client is prepared to send message to the server"
        def messageHandler = Mock(MessageHandler)
        def client = new TCPClient(converter).addHandler(messageHandler).connect("localhost", PORT).get()

        and: "incoming message stream for the pool is monitored"
        def listener = new TestObserver()
        clientPool.getServerMessages().subscribe(listener)

        when: "message is sent to the server"
        def sendingNode = UUID.randomUUID().toString()
        def receivingNode = UUID.randomUUID().toString()
        def message = new EmptyEnvelope(sendingNode, receivingNode)
        client.send(message)

        then: "message is received by the server and registered through pool handler"
        pollingConditions.eventually {
            listener.assertValueCount(1)
            def actualEvent = listener.values().first() as MessageWithClient
            assert actualEvent.getMessage() == message
        }

        and: "client for the sender exists in the pool"
        clientPool.exists(sendingNode)

        cleanup: "close client pool and server"
        clientPool.close()
        server.close()
    }

    def "Should register client under new ID when it accepts message from server"() {
        given: "server is running"
        def serverId = UUID.randomUUID().toString()
        def server = new TCPServer(converter).addHandler(buildEchoHandler(serverId)).bind(PORT).get()

        and: "client is connected and added to the pool"
        def clientId = UUID.randomUUID().toString()
        def client = clientPool.addUsingBuilder({
            handler -> new TCPClient(converter).addHandler(handler).connect("localhost", PORT)
        }).get()


        and: "incoming message stream for the pool is monitored"
        def listener = new TestObserver()
        clientPool.getClientMessages().subscribe(listener)

        when: "message is sent to the server"
        def message = new EmptyEnvelope(clientId, serverId)
        client.send(message)

        then: "message is sent back from the server to the client and pushed to the client message stream"
        def expectedMessage = new EmptyEnvelope(serverId, clientId)
        pollingConditions.eventually {
            listener.assertValueCount(1)
            def actualEvent = listener.values().first() as MessageWithClient
            assert actualEvent.getMessage() == expectedMessage
        }

        and: "client is associated with new node ID"
        clientPool.get(serverId) == Optional.of(client)

        cleanup: "close client pool and server"
        clientPool.close()
        server.close()
    }


    def buildEchoHandler(String senderId) {
        return new MessageHandler<EmptyEnvelope>() {
            @Override
            void handleMessage(EmptyEnvelope incomingMessage, Client client) {
                client.send(new EmptyEnvelope(senderId, incomingMessage.getSenderId()))
            }

            @Override
            Class getMessageType() {
                return EmptyEnvelope
            }
        }

    }

}
