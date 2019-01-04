package eu.phisikus.pivonia

import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.api.MessageHandler
import eu.phisikus.pivonia.api.TestMessage
import eu.phisikus.pivonia.api.middleware.Middleware
import eu.phisikus.pivonia.api.middleware.MiddlewareClient
import eu.phisikus.pivonia.converter.DaggerConverterComponent
import eu.phisikus.pivonia.crypto.CryptoModule
import eu.phisikus.pivonia.crypto.DaggerCryptoComponent
import eu.phisikus.pivonia.middleware.Cake
import eu.phisikus.pivonia.middleware.CakeWithClientPool
import eu.phisikus.pivonia.middleware.MissingMiddlewareException
import eu.phisikus.pivonia.middleware.layer.IdLayer
import eu.phisikus.pivonia.middleware.layer.ReturnLayer
import eu.phisikus.pivonia.tcp.DaggerTCPComponent
import eu.phisikus.pivonia.tcp.TCPComponent
import eu.phisikus.pivonia.test.CryptoUtils
import eu.phisikus.pivonia.test.ServerTestUtils
import io.vavr.NotImplementedError
import io.vavr.collection.Queue
import org.apache.tools.ant.util.FileUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

import java.time.Instant

class TimeServerTestITSpec extends Specification {

    @Shared
    def testKeyFilename

    @Shared
    int testServerPort

    final serverId = "server1"

    @Subject
    @Shared
    Cake<TimeMessage> cake

    @Shared
    Client client

    @Shared
    Queue<TimeMessage> messageQueue = Queue.empty()


    def "Should test time server"() {
        given: "Initial message is defined"
        def request = new TimeMessage("client1", serverId, 0L)

        when: "Message is sent"
        client.send(request).isSuccess()

        then: "Proper response is produced"
        messageQueue.dequeue()._1() == request

    }

    void cleanupSpec() {
        FileUtils.delete(new File(testKeyFilename))
    }


    void setupSpec() {
        final tcpComponent = buildIoCDependencies()
        cake = buildCake(tcpComponent)
        client = tcpComponent.getClientWithEncryption()
                .connect("localhost", testServerPort, new MessageHandler<TimeMessage>() {
            @Override
            void handleMessage(TimeMessage incomingMessage, Client client) {
                messageQueue.append(incomingMessage)
            }

            @Override
            Class getMessageType() {
                return TestMessage
            }
        }).get()
    }

    private def buildCake(TCPComponent tcpComponent) {
        def timeServerCake = new CakeWithClientPool(TimeMessage)
        def server = tcpComponent.getServerWithEncryption()
        testServerPort = ServerTestUtils.getRandomPort()
        timeServerCake.getClientPool().addSourceUsingBuilder({
            handler -> server.bind(testServerPort, handler)
        })
        timeServerCake.addLayer(new IdLayer(serverId))
        timeServerCake.addLayer(new TimeLayer())
        timeServerCake.addLayer(new ReturnLayer())
        timeServerCake.initialize()
        return timeServerCake
    }

    private TCPComponent buildIoCDependencies() {
        testKeyFilename = CryptoUtils.buildRandomKeyset()
        def testKeyContent = CryptoUtils.getKeysetContent(testKeyFilename)

        final cryptoComponent = DaggerCryptoComponent.builder()
                .cryptoModule(new CryptoModule(testKeyContent))
                .build()

        final converterComponent = DaggerConverterComponent.builder()
                .cryptoComponent(cryptoComponent)
                .build()

        return DaggerTCPComponent.builder()
                .converterComponent(converterComponent)
                .build()
    }


    private class TimeLayer implements Middleware<TimeMessage> {
        @Override
        void initialize(MiddlewareClient<TimeMessage> middlewareClient) throws MissingMiddlewareException {
        }

        @Override
        Optional<TimeMessage> handleIncomingMessage(TimeMessage message) {
            def timeMessage = new TimeMessage(
                    message.getRecipientId(),
                    message.getSenderId(),
                    Instant.now().toEpochMilli()
            )
            return Optional.of(timeMessage)
        }

        @Override
        Optional<TimeMessage> handleOutgoingMessage(TimeMessage message) {
            throw new NotImplementedError()
        }

        @Override
        void close() throws Exception {
        }
    }
}
