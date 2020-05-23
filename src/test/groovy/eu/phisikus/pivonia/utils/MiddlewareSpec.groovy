package eu.phisikus.pivonia.utils

import eu.phisikus.pivonia.logic.MessageHandler
import eu.phisikus.pivonia.logic.MessageHandlers
import spock.lang.Specification

class MiddlewareSpec extends Specification {
    def "Should register Middleware properly"() {
        given: "first middleware instance exists"
        def firstMiddleware = Mock(Middleware)
        def firstMiddlewareMessageHandler = GroovyMock(MessageHandler)
        def firstMiddlewareMessageHandlers = MessageHandlers.create().withHandler(firstMiddlewareMessageHandler)

        and: "second middleware instance exists"
        def secondMiddleware = Mock(Middleware)
        def secondMiddlewareMessageHandler = GroovyMock(MessageHandler)
        def secondMiddlewareMessageHandlers = MessageHandlers.create().withHandler(secondMiddlewareMessageHandler)

        and: "node ID is defined"
        def nodeId = UUID.randomUUID()

        and: "base message handlers are defined"
        def baseMessageHandler = GroovyMock(MessageHandler)
        def baseMessageHandlers = MessageHandlers.create().withHandler(baseMessageHandler)

        when: "middleware instances are injected into the node builder"
        def node = Node.builder()
                .id(nodeId)
                .messageHandlers(baseMessageHandlers)
                .middleware(firstMiddleware)
                .middleware(secondMiddleware)
                .build()

        then: "message handlers are extracted from middlewares"
        1 * firstMiddleware.getMessageHandlers() >> firstMiddlewareMessageHandlers
        1 * secondMiddleware.getMessageHandlers() >> secondMiddlewareMessageHandlers

        and: "all of the handlers are combined properly"
        node.messageHandlers.messageHandlers == [baseMessageHandler, firstMiddlewareMessageHandler, secondMiddlewareMessageHandler]

        and: "middlewares are initialized"
        1 * firstMiddleware.init(_)
        1 * secondMiddleware.init(_)
    }
}
