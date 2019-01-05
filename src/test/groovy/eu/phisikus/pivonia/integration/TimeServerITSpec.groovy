package eu.phisikus.pivonia.integration

import eu.phisikus.pivonia.converter.DaggerConverterComponent
import eu.phisikus.pivonia.crypto.CryptoModule
import eu.phisikus.pivonia.crypto.DaggerCryptoComponent
import eu.phisikus.pivonia.middleware.Cake
import eu.phisikus.pivonia.middleware.CakeWithClientPool
import eu.phisikus.pivonia.middleware.layer.IdLayer
import eu.phisikus.pivonia.middleware.layer.ReturnLayer
import eu.phisikus.pivonia.middleware.layer.TransformerLayer
import eu.phisikus.pivonia.tcp.DaggerTCPComponent
import eu.phisikus.pivonia.tcp.TCPComponent
import eu.phisikus.pivonia.test.CryptoUtils
import eu.phisikus.pivonia.test.ServerTestUtils
import org.apache.tools.ant.util.FileUtils
import spock.lang.Specification
import spock.lang.Subject
import spock.util.concurrent.PollingConditions

import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue

/**
 *
 * This test creates a "Cake" that works like a time server.
 * Client sends a message and proper timestamp is returned.
 * Both outgoing and incoming traffic are encrypted.
 *
 * Layers look like that:
 *
 * ClientPool (with connected server)
 * ^    |
 * |    v
 * IdLayer (sets the senderId on return)
 * ^    |
 * |    V
 * TimeLayer (sets the current timestamp)
 * ^    |
 * |    v
 * ReturnLayer
 *  ^---v
 *
 */
class TimeServerITSpec extends Specification {

    final serverId = "server1"
    final clientId = "client1"

    final messageQueue = new ConcurrentLinkedQueue<TimeMessage>()

    final timeLayer = new TransformerLayer<TimeMessage>(
            { message -> message },
            { message ->
                new TimeMessage(
                        message.getRecipientId(),
                        message.getSenderId(),
                        Instant.now().toEpochMilli()
                )
            }
    )

    def wait = new PollingConditions(timeout: 5)

    def "Should send message to the server and return with current timestamp"() {

        given: "IoC configuration is set up to provide networking with encryption"
        def (TCPComponent tcpComponent, String keysetFileName) = prepareIoCDependencies()

        and: "cake is created and connected to the TCP server"
        def testServerPort = ServerTestUtils.getRandomPort()
        def server = tcpComponent.getServerWithEncryption()
        def timeServerCake = new CakeWithClientPool(TimeMessage)
        timeServerCake.getClientPool().addSourceUsingBuilder({
            handler -> server.bind(testServerPort, handler)
        })


        and: "layers of algorithm are added to the cake"
        timeServerCake.addLayer(new IdLayer<String, TimeMessage>(serverId))
        timeServerCake.addLayer(timeLayer)
        timeServerCake.addLayer(new ReturnLayer<TimeMessage>())

        and: "cake is initialized"
        timeServerCake.initialize()

        and: "time request message is defined"
        def request = new TimeMessage(clientId, serverId, 0L)


        when: "message is sent to the server using encrypted client"
        def client = tcpComponent.getClientWithEncryption()
                .connect("localhost", testServerPort, new QueueMessageHandler(messageQueue))
                .get()

        client.send(request).isSuccess()

        then: "client receives response from the server with correct timestamp"
        wait.eventually {
            with(messageQueue.poll()) {
                senderId == serverId
                recipientId == clientId
                timestamp <= Instant.now().toEpochMilli()
            }
        }

        cleanup: "both client and server will be closed"
        client.close()
        timeServerCake.close()
        FileUtils.delete(new File(keysetFileName))
    }



    private def prepareIoCDependencies() {
        def testKeyFilename = CryptoUtils.buildRandomKeyset()
        def testKeyContent = CryptoUtils.getKeysetContent(testKeyFilename)

        final cryptoComponent = DaggerCryptoComponent.builder()
                .cryptoModule(new CryptoModule(testKeyContent))
                .build()

        final converterComponent = DaggerConverterComponent.builder()
                .cryptoComponent(cryptoComponent)
                .build()

        return new Tuple2<>(DaggerTCPComponent.builder()
                .converterComponent(converterComponent)
                .build(), testKeyFilename)
    }


}
