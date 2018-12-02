package eu.phisikus.pivonia.middleware.layer.pool

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


    def "Should register client when registered server accepts message from new node"() {
        given: "Server is added to the pool"
        def pollingConditions = new PollingConditions(timeout: 5)
        clientPool.addSourceUsingBuilder({
            handler -> new TCPServer(converter).bind(7979, handler)
        })

        and: "Client is prepared to send message to the server"
        def messageHandler = Mock(MessageHandler)
        def client = new TCPClient(converter).connect("localhost", 7979, messageHandler).get()

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
    }

    //TODO Add further tests
}
