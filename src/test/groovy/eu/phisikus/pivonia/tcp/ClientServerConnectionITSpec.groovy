package eu.phisikus.pivonia.tcp

import eu.phisikus.pivonia.api.Server
import eu.phisikus.pivonia.test.TestMessage
import eu.phisikus.pivonia.converter.plaintext.JacksonBSONConverter
import eu.phisikus.pivonia.test.ServerTestUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout

import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class ClientServerConnectionITSpec extends Specification {

    @Shared
    def bsonConverter = new JacksonBSONConverter()


    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    def "Client should be able to send message to server"() {
        given: "message and client are created"
        def testMessage = new TestMessage(Instant.now().toEpochMilli(), "test", "Test TestMessage")
        def client = new TCPClient(bsonConverter)
        def actualMessageHolder = new CompletableFuture<TestMessage>()

        when: "server is running and client is connected"
        def port = ServerTestUtils.getRandomPort()
        def server = startServer(port, actualMessageHolder)
        def connectedClient = client.connect("localhost", port).get()

        and: "message is sent using client"
        def messageSent = connectedClient.send(testMessage)

        then: "result is successful and message is received by the server"
        messageSent.isSuccess()
        actualMessageHolder.get() == testMessage

        cleanup: "close server and connected client"
        server.close()
        connectedClient.close()

    }

    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    def "Client should be able to send message to server and receive it back"() {
        given: "client is created and test message is defined"
        def testMessage = new TestMessage(new Date().getTime(), "test", "Test TestMessage")
        def client = new TCPClient(bsonConverter)
        def actualMessageHolder = new CompletableFuture<TestMessage>()
        def port = ServerTestUtils.getRandomPort()

        and: "server is started and client connected"
        def server = startEchoServer(port)
        def connectedClient = client
                .connect("localhost", port)
                .get()

        and: "incoming messages for the client will be monitored"
        connectedClient
                .getMessages(TestMessage)
                .subscribe({ event -> actualMessageHolder.complete(event.getMessage()) })

        when: "message is sent using client"
        def sendResult = connectedClient.send(testMessage)

        then: "sending is successful and the client received the message back"
        sendResult.isSuccess()
        actualMessageHolder.get() == testMessage

        cleanup: "close server and connected client"
        server.close()
        connectedClient.close()

    }


    private Server startServer(int port, messageReceivedLatch) {
        def server = new TCPServer(bsonConverter).bind(port).get()
        server.getMessages(TestMessage)
                .subscribe({ event -> messageReceivedLatch.complete(event.getMessage()) })
        return server
    }


    private Server startEchoServer(port) {
        def server = new TCPServer(bsonConverter)
                .bind(port)
                .get()
        server.getMessages(TestMessage)
                .subscribe({ event -> event.getTransmitter().send(event.getMessage()) })
        return server
    }
}
