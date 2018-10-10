package eu.phisikus.pivonia.middleware.test

import eu.phisikus.pivonia.api.MessageHandler
import eu.phisikus.pivonia.api.TestMessage
import eu.phisikus.pivonia.api.middleware.Middleware
import eu.phisikus.pivonia.api.middleware.MiddlewareClient
import eu.phisikus.pivonia.middleware.MissingMiddlewareException

import java.util.function.Consumer

class FirstMiddleware implements Middleware<TestMessage> {

    private MessageHandler<TestMessage> messageHandler
    private Consumer<TestMessage> messageConsumer

    FirstMiddleware(Consumer<TestMessage> messageConsumer) {
        this.messageConsumer = messageConsumer
    }

    @Override
    void initialize(MiddlewareClient<TestMessage> middlewareClient) throws MissingMiddlewareException {
        messageHandler = middlewareClient.getMessageHandler()
    }

    @Override
    Optional<TestMessage> handleIncomingMessage(TestMessage message) {
        return Optional.of(message)
    }

    @Override
    Optional<TestMessage> handleOutgoingMessage(TestMessage message) {
        messageConsumer.accept(message)
        return Optional.empty()
    }

    @Override
    void close() throws Exception {
        // nothing to do here
    }

    MessageHandler<TestMessage> getMessageHandler() {
        return messageHandler
    }

}