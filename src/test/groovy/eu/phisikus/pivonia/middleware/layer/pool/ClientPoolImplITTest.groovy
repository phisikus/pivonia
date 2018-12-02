package eu.phisikus.pivonia.middleware.layer.pool

import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.api.MessageHandler
import eu.phisikus.pivonia.api.pool.MessageWithClient
import eu.phisikus.pivonia.converter.DaggerConverterComponent
import eu.phisikus.pivonia.crypto.CryptoModule
import eu.phisikus.pivonia.crypto.DaggerCryptoComponent
import eu.phisikus.pivonia.middleware.layer.pool.test.FakeMessage
import eu.phisikus.pivonia.tcp.TCPClient
import eu.phisikus.pivonia.tcp.TCPServer
import io.reactivex.observers.TestObserver
import spock.lang.Specification
import spock.lang.Subject
import spock.util.concurrent.PollingConditions

class ClientPoolImplITTest extends Specification {

    @Subject
    def clientPool = new ClientPoolImpl(FakeMessage)

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

    def PORT = 7979

    def "Should register client when registered server accepts message from new node"() {
        given: "Server is added to the pool"
        def server = clientPool.addSourceUsingBuilder({
            handler -> new TCPServer(converter).bind(PORT, handler)
        }).get()

        and: "Client is prepared to send message to the server"
        def messageHandler = Mock(MessageHandler)
        def client = new TCPClient(converter).connect("localhost", PORT, messageHandler).get()

        and: "Incoming message stream for the pool is monitored"
        def listener = new TestObserver()
        clientPool.getServerMessages().subscribe(listener)

        when: "Message is sent to the server"
        def sendingNode = UUID.randomUUID()
        def receivingNode = UUID.randomUUID()
        def message = new FakeMessage(sendingNode, receivingNode)
        client.send(message)

        then: "Message is received by the server and registered through pool handler"
        pollingConditions.eventually {
            listener.assertValueCount(1)
            def actualEvent = listener.values().first() as MessageWithClient
            assert actualEvent.getMessage() == message
        }

        and: "Client for the sender exists in the pool"
        clientPool.exists(sendingNode)

        cleanup:
        clientPool.close()
        server.close()
    }

    def "Should register client under new ID when it accepts message from server"() {
        given: "Server is running"
        def serverId = UUID.randomUUID()
        def server = new TCPServer(converter).bind(PORT, buildEchoHandler(serverId)).get()

        and: "Client is connected and added to the pool"
        def clientId = UUID.randomUUID()
        def client = clientPool.addUsingBuilder({
            handler -> new TCPClient(converter).connect("localhost", PORT, handler)
        }).get()


        and: "Incoming message stream for the pool is monitored"
        def listener = new TestObserver()
        clientPool.getClientMessages().subscribe(listener)

        when: "Message is sent to the server"
        def message = new FakeMessage(clientId, serverId)
        client.send(message)

        then: "Message is sent back from the server to the client and pushed to the client message stream"
        def expectedMessage = new FakeMessage(serverId, clientId)
        pollingConditions.eventually {
            listener.assertValueCount(1)
            def actualEvent = listener.values().first() as MessageWithClient
            assert actualEvent.getMessage() == expectedMessage
        }

        and: "Client is associated with new node ID"
        clientPool.get(serverId) == Optional.of(client)

        cleanup:
        clientPool.close()
        server.close()
    }


    def buildEchoHandler(UUID senderId) {
        return new MessageHandler<FakeMessage>() {

            @Override
            void handleMessage(FakeMessage incomingMessage, Client client) {
                incomingMessage.setRecipientId(incomingMessage.getSenderId())
                incomingMessage.setSenderId(senderId)
                client.send(incomingMessage)
            }

            @Override
            Class getMessageType() {
                return FakeMessage
            }
        }

    }

}
