package eu.phisikus.pivonia.tcp

import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.api.MessageHandler
import eu.phisikus.pivonia.api.TestMessage
import eu.phisikus.pivonia.converter.plaintext.JacksonBSONConverter
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class ClientServerDataTransferSpec extends Specification {

    @Shared
    def bsonConverter = new JacksonBSONConverter()

    def "Message with a lot of data should be sent by client and received by server"() {
        given: "Server is running"
        def testMessage = new TestMessage(1L, "bigTopic", getBigMessage())
        def actualMessage = new CompletableFuture<TestMessage>()
        def server = new TCPServer(bsonConverter).bind(8092, buildMessageHandlerWithTrap(actualMessage))

        when: "Client is connected and message is sent"
        def client = new TCPClient(bsonConverter).connect("localhost", 8092, null).get()
        def sendResult = client.send(testMessage)

        then: "The operation finishes properly and received message is equal to expected"
        sendResult.isSuccess()
        actualMessage.get() == testMessage
    }

    def "Message with a lot of data should be sent by client to the server and back"() {
        given: "Server is running"
        def testMessage = new TestMessage(4L, "bigTopic", getBigMessage())
        def actualMessage = new CompletableFuture<TestMessage>()
        def server = new TCPServer(bsonConverter).bind(8093, ClientServerConnectionSpec.getEchoMessageHandler())

        when: "Client is connected and message is sent"
        def client = new TCPClient(bsonConverter).connect("localhost", 8093, buildMessageHandlerWithTrap(actualMessage)).get()
        def sendResult = client.send(testMessage)

        then: "The operation finishes properly and received message is equal to expected"
        sendResult.isSuccess()
        actualMessage.get() == testMessage
    }

    private MessageHandler buildMessageHandlerWithTrap(CompletableFuture<TestMessage> messageHolder) {
        new MessageHandler<TestMessage>() {
            @Override
            void handleMessage(TestMessage incomingMessage, Client client) {
                messageHolder.complete(incomingMessage)
            }

            @Override
            Class getMessageType() {
                TestMessage.class
            }
        }
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
