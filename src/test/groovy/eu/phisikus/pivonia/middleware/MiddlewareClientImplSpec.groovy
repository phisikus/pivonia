package eu.phisikus.pivonia.middleware

import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.api.TestMessage
import eu.phisikus.pivonia.api.middleware.Middleware
import io.vavr.collection.List
import spock.lang.Specification

class MiddlewareClientImplSpec extends Specification {

    final firstMessage = Mock(TestMessage)
    final secondMessage = Mock(TestMessage)
    final thirdMessage = Mock(TestMessage)

    final firstMiddleware = Mock(Middleware)
    final secondMiddleware = Mock(Middleware)
    final thirdMiddleware = Mock(Middleware)
    final middlewares = List.of(firstMiddleware, secondMiddleware, thirdMiddleware)

    final threeClients = List.of(
            new MiddlewareClientImpl(TestMessage, middlewares, 0),
            new MiddlewareClientImpl(TestMessage, middlewares, 1),
            new MiddlewareClientImpl(TestMessage, middlewares, 2)
    )
    final oneClient = List.of(
            new MiddlewareClientImpl(TestMessage, List.of(firstMiddleware), 0),
    )


    def "Should build client that can pass a message upwards"() {

        given: "there are three middleware layers with created clients"
        def clients = threeClients

        when: "the outgoing message handlers for the layers are spied on"
        1 * secondMiddleware.handleOutgoingMessage(firstMessage) >> Optional.of(secondMessage)
        1 * firstMiddleware.handleOutgoingMessage(secondMessage) >> Optional.of(thirdMessage)


        then: "sending the message calls all three handlers and passes the message around"
        clients.last().sendMessage(firstMessage)
    }


    def "Should build client that work with one layer"() {

        given: "there is one layer with created client"
        def clients = oneClient

        expect: "sending the message to not produce any errors"
        clients.last().sendMessage(firstMessage)
    }


    def "Should build client that stops passing message on empty return"() {

        given: "there are three middleware layers with created clients"
        def clients = threeClients

        when: "one of the message handlers return empty value"
        1 * secondMiddleware.handleOutgoingMessage(firstMessage) >> Optional.empty()
        0 * firstMiddleware.handleOutgoingMessage(_)


        then: "sending the message calls only the second client but not the first"
        clients.last().sendMessage(firstMessage)
    }

    def "Should build client that can pass a message downwards"() {

        given: "there are three middleware layers with created clients"
        def clients = threeClients

        when: "the ingoing message handlers for the layers are spied on"
        1 * firstMiddleware.handleIncomingMessage(firstMessage) >> Optional.of(secondMessage)
        1 * secondMiddleware.handleIncomingMessage(secondMessage) >> Optional.of(thirdMessage)
        1 * thirdMiddleware.handleIncomingMessage(thirdMessage) >> Optional.of(thirdMessage)


        then: "sending the message calls all three handlers and passes the message around"
        clients.first().getMessageHandler().handleMessage(firstMessage, Mock(Client))
    }

    def "Should build client that can stop passing message on empty return"() {

        given: "there are three middleware layers with created clients"
        def clients = threeClients

        when: "the second message client returns an empty value"
        1 * firstMiddleware.handleIncomingMessage(firstMessage) >> Optional.of(secondMessage)
        1 * secondMiddleware.handleIncomingMessage(secondMessage) >> Optional.empty()
        0 * thirdMiddleware.handleIncomingMessage(_)


        then: "sending the message will not pass the message to the third client"
        clients.first().getMessageHandler().handleMessage(firstMessage, Mock(Client))
    }


    def "Should build client that pass message through only one layer"() {

        given: "there are is one layer of middleware with created client"
        def clients = oneClient

        and: "message client is prepared"
        1 * firstMiddleware.handleIncomingMessage(firstMessage) >> Optional.of(secondMessage)

        expect: "sending the message to call that client"
        clients.first().getMessageHandler().handleMessage(firstMessage, Mock(Client))
    }

}
