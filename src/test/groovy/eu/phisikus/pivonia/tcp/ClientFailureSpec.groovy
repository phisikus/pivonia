package eu.phisikus.pivonia.tcp

import eu.phisikus.pivonia.api.TestMessage
import eu.phisikus.pivonia.converter.plaintext.JacksonBSONConverter
import eu.phisikus.pivonia.test.ServerTestUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout

import java.util.concurrent.TimeUnit

class ClientFailureSpec extends Specification {

    @Shared
    def bsonConverter = new JacksonBSONConverter()


    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    def "Client should report failure to send message when unconnected"() {
        given:
        def testMessage = new TestMessage()
        def client = new TCPClient(bsonConverter)

        when:
        def sendResult = client.send(testMessage)

        then:
        sendResult.isFailure()
    }

    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    def "Client should report failure to connect when server is unreachable"() {
        given:
        def client = new TCPClient(bsonConverter)

        when:
        def connectedClient = client.connect("localhost", ServerTestUtils.getRandomPort())

        then:
        connectedClient.isFailure()
    }

    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    def "Client should fail to send message when it was closed"() {
        given:
        def testMessage = new TestMessage()
        def port = ServerTestUtils.getRandomPort()
        def server = new TCPServer(bsonConverter).bind(port).get()
        def client = new TCPClient(bsonConverter).connect("localhost", port).get()

        when:
        client.close()
        def sendResult = client.send(testMessage)

        then:
        sendResult.isFailure()
    }
}
