package eu.phisikus.pivonia.middleware

import eu.phisikus.pivonia.api.MessageProcessor
import eu.phisikus.pivonia.api.Middleware
import eu.phisikus.pivonia.api.TestMessage
import spock.lang.Specification
import spock.lang.Subject

class CakeTest extends Specification {

    def "Should initialize all of its layers"() {

        given: "there are two layers of middleware and empty cake"
        def stateContainer = Mock(StateContainer)
        def firstMiddleware = Mock(Middleware)
        def secondMiddleware = Mock(Middleware)

        @Subject
        def cake = new Cake(stateContainer)

        and: "layers are configured to report initialization"
        1 * firstMiddleware.initialize(stateContainer)
        1 * secondMiddleware.initialize(stateContainer)

        when: "layers are added to the cake"
        cake
                .addLayer(firstMiddleware)
                .addLayer(secondMiddleware)


        then: "they are initialized with the whole cake"
        cake.initialize()
    }

    def "Should construct client message processor properly"() {

        given: "there is a cake"
        def stateContainer = Mock(StateContainer)

        @Subject
        def cake = new Cake(stateContainer)

        and: "two layers of middleware"
        def firstMiddleware = Mock(Middleware)
        def secondMiddleware = Mock(Middleware)
        def firstHandler = Mock(MessageProcessor)
        def secondHandler = Mock(MessageProcessor)
        firstMiddleware.getClientSideMessageProcessor() >> firstHandler
        secondMiddleware.getClientSideMessageProcessor() >> secondHandler


        and: "cake with those two layers initialized"
        cake
                .addLayer(firstMiddleware)
                .addLayer(secondMiddleware)
        cake.initialize()


        when: "client message processor is retrieved"
        def messageProcessor = cake.getClientSideMessageProcessor()


        and: "expectation is set that the processors will be called in chain"
        def fakeProcessedMessage = Mock(TestMessage)
        def fakeMessage = Mock(TestMessage)
        1 * firstHandler.processMessage(fakeMessage) >> Optional.of(fakeProcessedMessage)
        1 * secondHandler.processMessage(fakeProcessedMessage) >> Optional.of(fakeProcessedMessage)

        then: "that processor is called and handlers are called"
        messageProcessor.processMessage(fakeMessage) == Optional.of(fakeProcessedMessage)
    }

    def "Should construct server message processor properly"() {

        given: "there is a cake"
        def stateContainer = Mock(StateContainer)

        @Subject
        def cake = new Cake(stateContainer)

        and: "two layers of middleware"
        def firstMiddleware = Mock(Middleware)
        def secondMiddleware = Mock(Middleware)
        def firstHandler = Mock(MessageProcessor)
        def secondHandler = Mock(MessageProcessor)
        firstMiddleware.getServerSideMessageProcessor() >> firstHandler
        secondMiddleware.getServerSideMessageProcessor() >> secondHandler


        and: "cake with those two layers initialized"
        cake
                .addLayer(firstMiddleware)
                .addLayer(secondMiddleware)
        cake.initialize()


        when: "server message processor is retrieved"
        def messageProcessor = cake.getServerSideMessageProcessor()


        and: "expectation is set that the processors will be called in chain"
        def fakeProcessedMessage = Mock(TestMessage)
        def fakeMessage = Mock(TestMessage)
        1 * firstHandler.processMessage(fakeMessage) >> Optional.of(fakeProcessedMessage)
        1 * secondHandler.processMessage(fakeProcessedMessage) >> Optional.of(fakeProcessedMessage)

        then: "that processor is called and handlers are called"
        messageProcessor.processMessage(fakeMessage) == Optional.of(fakeProcessedMessage)
    }

    def "Should avoid calling the second message processor in chain if first gave an empty result"() {

        given: "there is a cake"
        def stateContainer = Mock(StateContainer)

        @Subject
        def cake = new Cake(stateContainer)

        and: "two layers of middleware"
        def firstMiddleware = Mock(Middleware)
        def secondMiddleware = Mock(Middleware)
        def firstHandler = Mock(MessageProcessor)
        def secondHandler = Mock(MessageProcessor)
        firstMiddleware.getServerSideMessageProcessor() >> firstHandler
        secondMiddleware.getServerSideMessageProcessor() >> secondHandler


        and: "cake with those two layers initialized"
        cake
                .addLayer(firstMiddleware)
                .addLayer(secondMiddleware)
        cake.initialize()


        when: "server message processor is retrieved"
        def messageProcessor = cake.getServerSideMessageProcessor()


        and: "expectation is set that the second processor will not be called"
        def fakeMessage = Mock(TestMessage)
        1 * firstHandler.processMessage(fakeMessage) >> Optional.empty()
        0 * secondHandler.processMessage(_) >> Optional.empty()

        then: "that processor is called and only one handler is called"
        messageProcessor.processMessage(fakeMessage) == Optional.empty()
    }

    def "Should throw an exception if layer of dependency is missing"() {

        given: "there is a cake with layer of middleware"
        def stateContainer = Mock(StateContainer)
        @Subject
        def cake = new Cake(stateContainer)
        def firstMiddleware = Mock(Middleware)
        cake.addLayer(firstMiddleware)

        and: "that one layer has unmet dependency"
        1 * firstMiddleware.initialize(stateContainer) >> { throw new MissingMiddlewareException(String.class) }

        when: "cake is initialized"
        cake.initialize()
        g
        then: "exception is thrown"
        thrown(MissingMiddlewareException)


    }
}
