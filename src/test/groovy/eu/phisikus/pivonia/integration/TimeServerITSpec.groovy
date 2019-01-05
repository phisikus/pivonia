package eu.phisikus.pivonia.integration

import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.converter.DaggerConverterComponent
import eu.phisikus.pivonia.crypto.CryptoModule
import eu.phisikus.pivonia.crypto.DaggerCryptoComponent
import eu.phisikus.pivonia.middleware.Cake
import eu.phisikus.pivonia.middleware.CakeWithClientPool
import eu.phisikus.pivonia.middleware.layer.IdLayer
import eu.phisikus.pivonia.middleware.layer.ReturnLayer
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


    @Subject
    Cake<TimeMessage> cake

    Client client

    String testKeyFilename

    int testServerPort

    def serverId = "server1"
    def clientId = "client1"


    final messageQueue = new ConcurrentLinkedQueue<TimeMessage>()

    def wait = new PollingConditions(timeout: 5)

    def "Should send message to the server and return with current timestamp set"() {
        given: "initial time message is defined"
        def request = new TimeMessage(clientId, serverId, 0L)

        when: "message is sent to the server"
        client.send(request).isSuccess()

        then: "client handles response from the server with correct timestamp set up"
        wait.eventually {
            with(messageQueue.poll()) {
                senderId == serverId
                recipientId == clientId
                timestamp <= Instant.now().toEpochMilli()
            }
        }

    }

    void cleanup() {
        cake.close()
        FileUtils.delete(new File(testKeyFilename))
    }


    void setup() {
        final tcpComponent = prepareIoCDependencies()
        cake = buildCake(tcpComponent)

        client = tcpComponent.getClientWithEncryption()
                .connect("localhost", testServerPort, new QueueMessageHandler(messageQueue))
                .get()
    }

    private def buildCake(TCPComponent tcpComponent) {
        testServerPort = ServerTestUtils.getRandomPort()

        def timeServerCake = new CakeWithClientPool(TimeMessage)
        def server = tcpComponent.getServerWithEncryption()
        timeServerCake.getClientPool().addSourceUsingBuilder({
            handler -> server.bind(testServerPort, handler)
        })
        timeServerCake.addLayer(new IdLayer<String, TimeMessage>(serverId))
        timeServerCake.addLayer(new TimeLayer())
        timeServerCake.addLayer(new ReturnLayer<TimeMessage>())
        timeServerCake.initialize()
        return timeServerCake
    }

    private TCPComponent prepareIoCDependencies() {
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


}
