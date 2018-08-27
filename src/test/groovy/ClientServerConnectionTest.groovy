import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.api.Message
import eu.phisikus.pivonia.api.MessageHandler
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

    private void startServer(messageReceivedLatch) {
        def messageHandler = new MessageHandler() {
            @Override
            void handleMessage(Message incomingMessage, Client client) {
                messageReceivedLatch.countDown()
            }
        }
        def server = new TCPServer(bsonConverter).bind(8090, messageHandler)
    }
}
