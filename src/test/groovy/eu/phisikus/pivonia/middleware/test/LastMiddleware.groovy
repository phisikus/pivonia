package eu.phisikus.pivonia.middleware.test

import eu.phisikus.pivonia.api.TestMessage
import eu.phisikus.pivonia.api.middleware.Middleware
import eu.phisikus.pivonia.api.middleware.MiddlewareClient
import eu.phisikus.pivonia.middleware.MissingMiddlewareException

class LastMiddleware implements Middleware<TestMessage> {

    private MiddlewareClient<TestMessage> client

    @Override
    void initialize(MiddlewareClient<TestMessage> middlewareClient) throws MissingMiddlewareException {
        client = middlewareClient
    }

    @Override
    Optional<TestMessage> handleIncomingMessage(TestMessage message) {
        client.sendMessage(message)
        return Optional.empty()
    }

    @Override
    Optional<TestMessage> handleOutgoingMessage(TestMessage message) {
        return Optional.of(message)
    }

    @Override
    void close() throws Exception {
        // nothing to do here
    }
}