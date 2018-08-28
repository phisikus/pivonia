package eu.phisikus.pivonia.tcp

import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.api.MessageHandler
import eu.phisikus.pivonia.api.Server
import eu.phisikus.pivonia.api.TestMessage
import eu.phisikus.pivonia.converter.JacksonBSONConverter
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class ClientServerConnectionTest extends Specification {

    @Shared
    def bsonConverter = new JacksonBSONConverter()

    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    def "Client should be able to send message to server"() {
        given:
        def testMessage = new TestMessage(new Date().getTime(), "test", "Test TestMessage")
        def client = new TCPClient(bsonConverter)
        def actualMessageHolder = new CompletableFuture<TestMessage>()

        when:
        startServer(actualMessageHolder)
        def messageSent = client.connect("localhost", 8090, null).get().send(testMessage)

        then:
        messageSent.isSuccess()
        actualMessageHolder.get() == testMessage

    }

    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    def "Client should be able to send message to server and receive it back"() {
        given:
        def testMessage = new TestMessage(new Date().getTime(), "test", "Test TestMessage")
        def client = new TCPClient(bsonConverter)
        def actualMessageHolder = new CompletableFuture<TestMessage>()
        when:
        startEchoServer()
        def connectedClient = client
                .connect("localhost", 8091, getFutureCompletingHandler(actualMessageHolder))
                .get()
        def sendResult = connectedClient.send(testMessage)

        then:
        sendResult.isSuccess()
        actualMessageHolder.get() == testMessage

    }


    private Server startServer(messageReceivedLatch) {
        MessageHandler messageHandler = getFutureCompletingHandler(messageReceivedLatch)
        return new TCPServer(bsonConverter).bind(8090, messageHandler) as Server
    }

    private MessageHandler getFutureCompletingHandler(messageHolder) {
        def messageHandler = new MessageHandler<TestMessage>() {
            @Override
            void handleMessage(TestMessage incomingMessage, Client client) {
                messageHolder.complete(incomingMessage)
            }

            @Override
            Class<TestMessage> getMessageType() {
                return TestMessage.class
            }
        }
        messageHandler
    }

    private Server startEchoServer() {
        MessageHandler<TestMessage> messageHandler = getEchoMessageHandler()
        return new TCPServer(bsonConverter).bind(8091, messageHandler) as Server
    }

    static MessageHandler<TestMessage> getEchoMessageHandler() {
        def messageHandler = new MessageHandler<TestMessage>() {
            @Override
            void handleMessage(TestMessage incomingMessage, Client client) {
                client.send(incomingMessage)
            }

            @Override
            Class<TestMessage> getMessageType() {
                return TestMessage.class
            };
        }
        messageHandler
    }
}
