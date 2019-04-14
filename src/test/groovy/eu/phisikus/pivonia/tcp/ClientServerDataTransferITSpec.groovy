package eu.phisikus.pivonia.tcp

import eu.phisikus.pivonia.api.TestMessage
import eu.phisikus.pivonia.converter.plaintext.JacksonBSONConverter
import eu.phisikus.pivonia.test.ServerTestUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class ClientServerDataTransferITSpec extends Specification {


    @Shared
    def bsonConverter = new JacksonBSONConverter()

    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    def "Message with a lot of data should be sent by client and received by server"() {
        given: "server is running"
        def testMessage = new TestMessage(1L, "bigTopic", getBigMessage())
        def actualMessage = new CompletableFuture<TestMessage>()
        def port = ServerTestUtils.getRandomPort()
        def server = new TCPServer(bsonConverter)
                .bind(port)
                .get()

        and: "it is configured to monitor incoming messages"
        server.getMessages(TestMessage)
                .subscribe({ event -> actualMessage.complete(event.getMessage()) })

        when: "transmitter is connected and message is sent"
        def client = new TCPClient(bsonConverter).connect("localhost", port).get()
        def sendResult = client.send(testMessage)

        then: "the operation finishes properly and received message is equal to expected"
        sendResult.isSuccess()
        actualMessage.get() == testMessage

        cleanup: "close the server and transmitter"
        server.close()
        client.close()
    }

    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    def "Message with a lot of data should be sent by client to the server and back"() {
        given: "the server is running"
        def testMessage = new TestMessage(4L, "bigTopic", getBigMessage())
        def actualMessage = new CompletableFuture<TestMessage>()
        def port = ServerTestUtils.getRandomPort()
        def server = new TCPServer(bsonConverter)
                .bind(port)
                .get()

        and: "it is set up to send back incoming messages"
        def echoHandler = { event -> event.getTransmitter().send(event.getMessage()) }
        server.getMessages(TestMessage)
                .subscribe(echoHandler)

        when: "transmitter is connected"
        def client = new TCPClient(bsonConverter)
                .connect("localhost", port)
                .get()

        and: "transmitter is configured to monitor incoming messages"
        client.getMessages(TestMessage)
                .subscribe({ event -> actualMessage.complete(event.getMessage()) })

        and: "message is sent"
        def sendResult = client.send(testMessage)

        then: "the operation finishes properly and received message is equal to expected"
        sendResult.isSuccess()
        actualMessage.get() == testMessage

        cleanup: "close the server and transmitter"
        server.close()
        client.close()
    }

    private String getBigMessage() {
        def message = new StringBuilder()
        def randomGenerator = new Random()
        def addRandomInt = {
            message.append(randomGenerator.nextInt())
        }
        100_000.times(addRandomInt)
        message.toString()
    }
}
