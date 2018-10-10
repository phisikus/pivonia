package eu.phisikus.pivonia.middleware.test

import eu.phisikus.pivonia.api.TestMessage
import eu.phisikus.pivonia.api.middleware.Middleware
import eu.phisikus.pivonia.api.middleware.MiddlewareClient
import eu.phisikus.pivonia.middleware.MissingMiddlewareException

class MiddleMiddleware implements Middleware<TestMessage> {
    private int layerNo

    MiddleMiddleware(int layerNo) {
        this.layerNo = layerNo
    }

    @Override
    void initialize(MiddlewareClient<TestMessage> middlewareClient) throws MissingMiddlewareException {
        // nothing to do here
    }

    @Override
    Optional<TestMessage> handleIncomingMessage(TestMessage message) {
        final newMessage = new TestMessage(message.getTimestamp() + 1,
                message.getTopic(),
                message.getMessage() + "+" + layerNo)
        return Optional.of(newMessage)
    }

    @Override
    Optional<TestMessage> handleOutgoingMessage(TestMessage message) {
        final newMessage = new TestMessage(
                message.getTimestamp() + 1,
                message.getTopic(),
                message.getMessage() + "-" + layerNo)
        return Optional.of(newMessage)
    }

    @Override
    void close() throws Exception {
        // nothing to do here
    }
}
