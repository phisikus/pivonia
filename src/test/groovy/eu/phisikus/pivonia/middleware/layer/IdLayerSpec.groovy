package eu.phisikus.pivonia.middleware.layer

import eu.phisikus.pivonia.middleware.layer.pool.test.FakeMessage
import spock.lang.Specification
import spock.lang.Subject

class IdLayerSpec extends Specification {

    @Subject
    def idLayer = new IdLayer(UUID.randomUUID())

    def "Should set sender ID for outgoing message"() {
        given: "there is a message with defined recipient"
        final message = new FakeMessage(null, UUID.randomUUID())

        when: "handling outgoing message"
        def actualMessage = idLayer.handleOutgoingMessage(message)

        then: "returned message has expected sender ID"
        actualMessage.get().getSenderId() == idLayer.getId()
    }

    def "Should pass message with correct recipient ID"() {
        given: "there is a message with correct recipient ID"
        final message = new FakeMessage(null, idLayer.getId())

        when: "handling incoming message"
        def actualMessage = idLayer.handleIncomingMessage(message)

        then: "message is passed properly"
        actualMessage.isPresent()
        actualMessage.get() == message
    }

    def "Should reject messages without matching recipient ID"() {
        given: "there is a message with correct recipient ID"
        final message = new FakeMessage(null, UUID.randomUUID())

        when: "handling incoming message"
        def actualMessage = idLayer.handleIncomingMessage(message)

        then: "message is not passed"
        !actualMessage.isPresent()
    }
}
