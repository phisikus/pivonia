package eu.phisikus.pivonia.tcp

import eu.phisikus.pivonia.test.TestMessage
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
        given: "test message and unconnected transmitter are defined"
        def testMessage = new TestMessage(1L, "", "")
        def client = new TCPClient(bsonConverter)

        when: "sending the message using transmitter"
        def sendResult = client.send(testMessage)

        then: "failure is reported"
        sendResult.isFailure()
    }

    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    def "Client should report failure to connect when server is unreachable"() {
        given: "transmitter is defined"
        def client = new TCPClient(bsonConverter)

        when: "connection to some random port is preformed"
        def connectedClient = client.connect("localhost", ServerTestUtils.getRandomPort())

        then: "transmitter reports failure"
        connectedClient.isFailure()
    }

    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    def "Client should fail to send message when it was closed"() {
        given: "server and transmitter are created"
        def testMessage = new TestMessage(1L, "", "")
        def port = ServerTestUtils.getRandomPort()
        def server = new TCPServer(bsonConverter).bind(port).get()
        def client = new TCPClient(bsonConverter).connect("localhost", port).get()

        when: "transmitter is closed and message sent"
        client.close()
        def sendResult = client.send(testMessage)

        then: "transmitter should report failure"
        sendResult.isFailure()

        cleanup:
        server.close()
    }
}
