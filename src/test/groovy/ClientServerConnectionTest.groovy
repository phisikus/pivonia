import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.api.Message
import eu.phisikus.pivonia.api.MessageHandler
import eu.phisikus.pivonia.api.Server
import eu.phisikus.pivonia.converter.JacksonBSONConverter
import eu.phisikus.pivonia.tcp.TCPClient
import eu.phisikus.pivonia.tcp.TCPServer
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ClientServerConnectionTest extends Specification {

    @Shared
    def bsonConverter = new JacksonBSONConverter()

    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    def "Client should be able to send message to server"() {
        given:
        def testMessage = new Message(new Date().getTime(), "test", "Test Message")
        def client = new TCPClient(bsonConverter)
        def messageReceivedLatch = new CountDownLatch(1)

        when:
        startServer(messageReceivedLatch)
        def messageSent = client.connect("localhost", 8090, null).get().send(testMessage)

        then:
        messageSent.isSuccess()
        messageReceivedLatch.await()

    }

    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    def "Client should be able to send message to server and receive it back"() {
        given:
        def testMessage = new Message(new Date().getTime(), "test", "Test Message")
        def client = new TCPClient(bsonConverter)
        def messageReceivedLatch = new CountDownLatch(1)

        when:
        startEchoServer()
        def connectedClient = client
                .connect("localhost", 8091, getLatchTriggeringHandler(messageReceivedLatch))
                .get()

        def sendResult = connectedClient.send(testMessage)

        then:
        sendResult.isSuccess()
        messageReceivedLatch.await()

    }

    private MessageHandler getLatchTriggeringHandler(messageReceivedLatch) {
        def messageHandler = new MessageHandler() {
            @Override
            void handleMessage(Message incomingMessage, Client client) {
                messageReceivedLatch.countDown()
            }
        }
        messageHandler
    }

    private Server startServer(messageReceivedLatch) {
        MessageHandler messageHandler = getLatchTriggeringHandler(messageReceivedLatch)
        return new TCPServer(bsonConverter).bind(8090, messageHandler) as Server
    }

    private Server startEchoServer() {
        def messageHandler = new MessageHandler() {
            @Override
            void handleMessage(Message incomingMessage, Client client) {
                client.send(incomingMessage)
            }
        }
        return new TCPServer(bsonConverter).bind(8091, messageHandler) as Server
    }
}
