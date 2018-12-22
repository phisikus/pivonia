package eu.phisikus.pivonia.tcp

import eu.phisikus.pivonia.ServerTestUtils
import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.api.MessageHandler
import eu.phisikus.pivonia.api.Server
import eu.phisikus.pivonia.api.TestMessage
import eu.phisikus.pivonia.converter.plaintext.JacksonBSONConverter
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class ClientServerConnectionSpec extends Specification {

    @Shared
    def bsonConverter = new JacksonBSONConverter()

    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    def "Client should be able to send message to server"() {
        given:
        def testMessage = new TestMessage(new Date().getTime(), "test", "Test TestMessage")
        def client = new TCPClient(bsonConverter)
        def actualMessageHolder = new CompletableFuture<TestMessage>()

        when:
        def port = ServerTestUtils.getRandomPort()
        def server = startServer(port, actualMessageHolder)
        def messageSent = client.connect("localhost", port, null).get().send(testMessage)

        then:
        messageSent.isSuccess()
        actualMessageHolder.get() == testMessage

        cleanup:
        server.close()

    }

    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    def "Client should be able to send message to server and receive it back"() {
        given:
        def testMessage = new TestMessage(new Date().getTime(), "test", "Test TestMessage")
        def client = new TCPClient(bsonConverter)
        def actualMessageHolder = new CompletableFuture<TestMessage>()
        def port = ServerTestUtils.getRandomPort()

        when:
        def server = startEchoServer(port)
        def connectedClient = client
                .connect("localhost", port, getFutureCompletingHandler(actualMessageHolder))
                .get()
        def sendResult = connectedClient.send(testMessage)

        then:
        sendResult.isSuccess()
        actualMessageHolder.get() == testMessage

        cleanup:
        server.close()

    }


    private Server startServer(int port, messageReceivedLatch) {
        MessageHandler messageHandler = getFutureCompletingHandler(messageReceivedLatch)
        return new TCPServer(bsonConverter).bind(port, messageHandler).get()
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

    private Server startEchoServer(port) {
        MessageHandler<TestMessage> messageHandler = getEchoMessageHandler()
        return new TCPServer(bsonConverter).bind(port, messageHandler).get()
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
