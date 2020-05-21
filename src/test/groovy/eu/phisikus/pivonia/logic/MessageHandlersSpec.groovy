package eu.phisikus.pivonia.logic

import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.api.MessageWithTransmitter
import eu.phisikus.pivonia.api.Server
import eu.phisikus.pivonia.utils.Node
import io.reactivex.subjects.PublishSubject
import spock.lang.Specification

import java.util.function.BiConsumer

class MessageHandlersSpec extends Specification {

    def "Should create MessageHandlers with multiple handlers and register them in client"() {
        given: "there are two Message Handler definitions"
        def messagesObserved = []
        def context = Mock(Node)
        def firstConsumer = { Node ctx, FirstType message -> messagesObserved.add(message) } as BiConsumer<Node, FirstType>
        def secondConsumer = { Node ctx, SecondType message -> messagesObserved.add(message) } as BiConsumer<Node, SecondType>
        def firstHandler = MessageHandler.create(FirstType, firstConsumer)
        def secondHandler = MessageHandler.create(SecondType, secondConsumer)

        and: "test messages for message handlers are defined"
        def firstMessage = Mock(FirstType)
        def secondMessage = Mock(SecondType)
        def firstHandlerMessage = new MessageWithTransmitter(firstMessage, null)
        def secondHandlerMessage = new MessageWithTransmitter(secondMessage, null)

        and: "configured client is prepared"
        def client = Mock(Client)
        def firstHandlerSource = PublishSubject.create()
        def secondHandlerSource = PublishSubject.create()
        1 * client.getMessages(FirstType) >> firstHandlerSource
        1 * client.getMessages(SecondType) >> secondHandlerSource

        when: "creating MessageHandlers instance"
        def handlers = MessageHandlers.create()
                .withHandler(firstHandler)
                .withHandler(secondHandler)
                .build(context)

        and: "calling for client registration"
        handlers.registerHandlers(client)

        then: "message streams for both specific handlers are registered"
        firstHandlerSource.hasObservers()
        secondHandlerSource.hasObservers()

        and: "incoming messages are properly passed to handlers"
        firstHandlerSource.onNext(firstHandlerMessage)
        secondHandlerSource.onNext(secondHandlerMessage)
        messagesObserved == [firstMessage, secondMessage]

        cleanup: "Message Handlers is disposed"
        handlers.dispose()
    }


    def "Should create MessageHandlers with multiple handlers and register them in server"() {
        given: "there are two Message Handler definitions"
        def messagesObserved = []
        def context = Mock(Node)
        def firstConsumer = { Node ctx, FirstType message -> messagesObserved.add(message) } as BiConsumer<Node, FirstType>
        def secondConsumer = { Node ctx, SecondType message -> messagesObserved.add(message) } as BiConsumer<Node, SecondType>
        def firstHandler = MessageHandler.create(FirstType, firstConsumer)
        def secondHandler = MessageHandler.create(SecondType, secondConsumer)

        and: "test messages for message handlers are defined"
        def firstMessage = Mock(FirstType)
        def secondMessage = Mock(SecondType)
        def firstHandlerMessage = new MessageWithTransmitter(firstMessage, null)
        def secondHandlerMessage = new MessageWithTransmitter(secondMessage, null)

        and: "configured server is prepared"
        def server = Mock(Server)
        def firstHandlerSource = PublishSubject.create()
        def secondHandlerSource = PublishSubject.create()
        1 * server.getMessages(FirstType) >> firstHandlerSource
        1 * server.getMessages(SecondType) >> secondHandlerSource

        when: "creating MessageHandlers instance"
        def handlers = MessageHandlers.create()
                .withHandler(firstHandler)
                .withHandler(secondHandler)
                .build(context)

        and: "calling for registration in server"
        handlers.registerHandlers(server)

        then: "message streams for both specific handlers are registered"
        firstHandlerSource.hasObservers()
        secondHandlerSource.hasObservers()

        and: "incoming messages are properly passed to handlers"
        firstHandlerSource.onNext(firstHandlerMessage)
        secondHandlerSource.onNext(secondHandlerMessage)
        messagesObserved == [firstMessage, secondMessage]

        cleanup: "Message Handlers is disposed"
        handlers.dispose()
    }

    def "Should combine two MessageHandlers properly"() {
        given: "first MessageHandlers is defined"
        def firstHandler = createTestHandler()
        def secondHandler = createTestHandler()
        def context = Mock(Object)
        def firstMessageHandlers = MessageHandlers.create()
                .withHandler(firstHandler)
                .withHandler(secondHandler)
                .build(context)

        and: "second MessageHandlers is defined"
        def thirdHandler = createTestHandler()
        def fourthHandler = createTestHandler()
        def secondMessageHandlers = MessageHandlers.create()
                .withHandler(thirdHandler)
                .withHandler(fourthHandler)
                .build(context)

        when: "merge function is called"
        def mergedMessageHandlers = firstMessageHandlers.withHandlers(secondMessageHandlers)

        then: "all of the original MessageHandler instances are included"
        mergedMessageHandlers.messageHandlers == [firstHandler, secondHandler, thirdHandler, fourthHandler]
    }

    private MessageHandler<Object, Object> createTestHandler() {
        MessageHandler.create(Object, {})
    }

    private class FirstType {}

    private class SecondType {}
}
