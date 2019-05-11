package eu.phisikus.pivonia.logic


import spock.lang.Specification

import java.util.function.BiConsumer

class MessageHandlerSpec extends Specification {

    def "Should create new MessageHandler for given type and message consumer"() {
        given: "message type and consuming function is defined"
        def type = Object
        def consumer = Mock(BiConsumer)

        when: "calling builder method of MessageHandler"
        def messageHandler = MessageHandler.create(type, consumer)

        then: "handler contains provided class type and handling function"
        verifyAll(messageHandler) {
            getMessageType() == type
            getMessageHandler() == consumer
        }
    }

    def "Should not create MessageHandler for null messageType"() {
        when: "calling builder method of MessageHandler with null messageType"
        MessageHandler.create(null, Mock(BiConsumer))

        then: "exception should be thrown"
        thrown IllegalArgumentException
    }

    def "Should not create MessageHandler for null consumer"() {
        when: "calling builder method of MessageHandler with null consumer"
        MessageHandler.create(Object, null)

        then: "exception should be thrown"
        thrown IllegalArgumentException
    }
}
