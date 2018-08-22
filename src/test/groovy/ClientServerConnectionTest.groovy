import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.api.Message
import eu.phisikus.pivonia.api.MessageHandler
import eu.phisikus.pivonia.tcp.TCPClient
import eu.phisikus.pivonia.tcp.TCPServer
import spock.lang.Specification

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
