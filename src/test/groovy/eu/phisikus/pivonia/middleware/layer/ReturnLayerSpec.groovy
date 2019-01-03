package eu.phisikus.pivonia.middleware.layer

import eu.phisikus.pivonia.api.TestMessage
import eu.phisikus.pivonia.api.middleware.MiddlewareClient
import spock.lang.Specification
import spock.lang.Subject

class ReturnLayerSpec extends Specification {

    @Subject
    def returnLayer = new ReturnLayer()

    def "Should forward incoming message through client"() {
        given: "message is defined"
        def message = new TestMessage()

        and: "middleware client is mocked"
        def client = Mock(MiddlewareClient)
        returnLayer.initialize(client)

        when: "incoming message is passed"
        returnLayer.handleIncomingMessage(message)

        then: "client is called to send the message"
        1 * client.sendMessage(message)
    }
}
