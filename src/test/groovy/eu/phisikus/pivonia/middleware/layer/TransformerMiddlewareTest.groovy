package eu.phisikus.pivonia.middleware.layer

import eu.phisikus.pivonia.api.TestMessage
import eu.phisikus.pivonia.api.middleware.MiddlewareClient
import spock.lang.Specification

import java.util.function.Function

class TransformerMiddlewareTest extends Specification {

    def "Middleware should be created and initialized properly"() {
        given: "instance of the middleware is created"
        def incomingConverter = Mock(Function)
        def outgoingConverter = Mock(Function)
        def middleware = new TransformerMiddleware(incomingConverter, outgoingConverter)

        expect: "the initializing function to complete without errors"
        middleware.initialize(Mock(MiddlewareClient))
    }


    def "Middleware should apply given function to incoming messages"() {
        given: "instance of the middleware is created"
        def incomingConverter = Mock(Function)
        def outgoingConverter = Mock(Function)
        def middleware = new TransformerMiddleware(incomingConverter, outgoingConverter)

        and: "test message is defined"
        def testMessage = Mock(TestMessage)
        def expectedMessage = Mock(TestMessage)

        when: "calling for the incoming message handler"
        1 * incomingConverter.apply(testMessage) >> expectedMessage
        def actualResult = middleware.handleIncomingMessage(testMessage)

        then: "function returns processed message"
        actualResult == Optional.of(expectedMessage)
    }

    def "Middleware should apply given function to outgoing messages"() {
        given: "instance of the middleware is created"
        def incomingConverter = Mock(Function)
        def outgoingConverter = Mock(Function)
        def middleware = new TransformerMiddleware(incomingConverter, outgoingConverter)

        and: "test message is defined"
        def testMessage = Mock(TestMessage)
        def expectedMessage = Mock(TestMessage)

        when: "calling for the outgoing message handler"
        1 * outgoingConverter.apply(testMessage) >> expectedMessage
        def actualResult = middleware.handleOutgoingMessage(testMessage)

        then: "function returns processed message"
        actualResult == Optional.of(expectedMessage)
    }
}