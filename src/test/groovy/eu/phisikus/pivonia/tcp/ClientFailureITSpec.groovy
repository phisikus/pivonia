package eu.phisikus.pivonia.tcp

import eu.phisikus.pivonia.api.TestMessage
import eu.phisikus.pivonia.converter.plaintext.JacksonBSONConverter
import eu.phisikus.pivonia.test.ServerTestUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout

import java.util.concurrent.TimeUnit

class ClientFailureITSpec extends Specification {

    @Shared
    def bsonConverter = new JacksonBSONConverter()


    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    def "Client should report failure to send message when unconnected"() {
        given: "test message and unconnected client are defined"
        def testMessage = new TestMessage()
        def client = new TCPClient(bsonConverter)

        when: "sending the message using client"
        def sendResult = client.send(testMessage)

        then: "failure is reported"
        sendResult.isFailure()
    }

    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    def "Client should report failure to connect when server is unreachable"() {
        given: "client is defined"
        def client = new TCPClient(bsonConverter)

        when: "connection to some random port is preformed"
        def connectedClient = client.connect("localhost", ServerTestUtils.getRandomPort())

        then: "client reports failure"
        connectedClient.isFailure()
    }

    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    def "Client should fail to send message when it was closed"() {
        given: "server and client are created"
        def testMessage = new TestMessage()
        def port = ServerTestUtils.getRandomPort()
        def server = new TCPServer(bsonConverter).bind(port).get()
        def client = new TCPClient(bsonConverter).connect("localhost", port).get()

        when: "client is closed and message sent"
        client.close()
        def sendResult = client.send(testMessage)

        then: "client should report failure"
        sendResult.isFailure()

        cleanup:
        server.close()
    }
}
