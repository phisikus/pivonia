package eu.phisikus.pivonia.middleware

import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.api.TestMessage
import eu.phisikus.pivonia.middleware.test.FirstMiddleware
import eu.phisikus.pivonia.middleware.test.LastMiddleware
import eu.phisikus.pivonia.middleware.test.MiddleMiddleware
import spock.lang.Specification
import spock.lang.Subject

import java.util.function.Consumer

class CakeIntegrationTest extends Specification {

    @Subject
    def cake = new Cake(TestMessage)

    def "Cake should process messages sent by initialized layers"() {
        given: "there is a cake with 5 layers that produces message for each one sent"
        def messageReceiver = Mock(Consumer)
        def firstMiddleware = new FirstMiddleware(messageReceiver)
        cake.addLayer(firstMiddleware)
                .addLayer(new MiddleMiddleware(2))
                .addLayer(new MiddleMiddleware(3))
                .addLayer(new MiddleMiddleware(4))
                .addLayer(new LastMiddleware())

        and: "some test messages"
        def testMessage = new TestMessage(0L, "general", "Hello ")
        def expectedMessage = new TestMessage(6, "general", "Hello +2+3+4-4-3-2")

        when: "the receiver is set up to catch and verify incoming message"
        1 * messageReceiver.accept(expectedMessage) >> println(expectedMessage)

        then: "the message should return to the receiver once it is sent into the system"
        cake.initialize()
        firstMiddleware.getMessageHandler().handleMessage(testMessage, Mock(Client))

    }

}
