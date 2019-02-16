package eu.phisikus.pivonia.logic

import spock.lang.Specification

import java.util.function.Consumer

class MessageHandlerSpec extends Specification {

    def "Should create new MessageHandler for given type and message consumer"() {
        given: "message type and consuming function is defined"
        def type = Object
        def consumer = Mock(Consumer)

        when: "calling builder method of MessageHandler"
        def messageHandler = MessageHandler.create(type, consumer)

        then: "handler contains provided class type and handling function"
        verifyAll(messageHandler) {
            getMessageType() == type
            getMessageHandler() == consumer
        }
    }
}
