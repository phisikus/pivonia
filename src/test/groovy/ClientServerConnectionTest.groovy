import eu.phisikus.pivonia.Client
import eu.phisikus.pivonia.Message
import eu.phisikus.pivonia.MessageHandler
import eu.phisikus.pivonia.TCPClient
import eu.phisikus.pivonia.TCPServer
import spock.lang.Specification

import java.util.function.Consumer

class ClientServerConnectionTest extends Specification {

    def "Client should be able to connect to server"() {
        given:
        def server = new TCPServer(8090, new MessageHandler() {
            @Override
            void handleMessage(Message incomingMessage, Client client) {
                println incomingMessage
            }
        })
        def client = new TCPClient("localhost", 8090)
        client.send(new Message(new Date().getTime(), "test", "Test Message"))
        Thread.sleep(1000) // TODO remove this
    }
}
