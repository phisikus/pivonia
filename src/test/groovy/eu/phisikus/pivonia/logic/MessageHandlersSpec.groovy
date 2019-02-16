package eu.phisikus.pivonia.logic

import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.api.Server
import io.reactivex.subjects.PublishSubject
import spock.lang.Specification

import java.util.concurrent.TimeUnit

class MessageHandlersSpec extends Specification {

    private class FirstType {}

    private class SecondType {}

    def "Should create message handler with multiple handlers and register them in client"() {
        given: "there are two Message Handler definitions"
        def messagesObserved = []
        def firstConsumer = { FirstType message -> messagesObserved.add(message) }
        def secondConsumer = { SecondType message -> messagesObserved.add(message) }
        def firstHandler = MessageHandler.create(FirstType, firstConsumer)
        def secondHandler = MessageHandler.create(SecondType, secondConsumer)

        and: "test messages for message handlers are defined"
        def firstHandlerMessage = Mock(FirstType)
        def secondHandlerMessage = Mock(SecondType)

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
                .build()

        and: "calling for client registration"
        handlers.registerHandlers(client)

        then: "message streams for both specific handlers are registered"
        firstHandlerSource.hasObservers()
        secondHandlerSource.hasObservers()

        and: "incoming messages are properly passed to handlers"
        firstHandlerSource.onNext(firstHandlerMessage)
        secondHandlerSource.onNext(secondHandlerMessage)
        messagesObserved == [firstHandlerMessage, secondHandlerMessage]

        cleanup: "Message Handlers is disposed"
        handlers.dispose()
    }


    def "Should create message handler with multiple handlers and register them in server"() {
        given: "there are two Message Handler definitions"
        def messagesObserved = []
        def firstConsumer = { FirstType message -> messagesObserved.add(message) }
        def secondConsumer = { SecondType message -> messagesObserved.add(message) }
        def firstHandler = MessageHandler.create(FirstType, firstConsumer)
        def secondHandler = MessageHandler.create(SecondType, secondConsumer)

        and: "test messages for message handlers are defined"
        def firstHandlerMessage = Mock(FirstType)
        def secondHandlerMessage = Mock(SecondType)

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
                .build()

        and: "calling for registration in server"
        handlers.registerHandlers(server)

        then: "message streams for both specific handlers are registered"
        firstHandlerSource.hasObservers()
        secondHandlerSource.hasObservers()

        and: "incoming messages are properly passed to handlers"
        firstHandlerSource.onNext(firstHandlerMessage)
        secondHandlerSource.onNext(secondHandlerMessage)
        messagesObserved == [firstHandlerMessage, secondHandlerMessage]

        cleanup: "Message Handlers is disposed"
        handlers.dispose()
    }
}
