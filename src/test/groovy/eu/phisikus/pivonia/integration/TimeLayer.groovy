package eu.phisikus.pivonia.integration

import eu.phisikus.pivonia.api.middleware.Middleware
import eu.phisikus.pivonia.api.middleware.MiddlewareClient
import eu.phisikus.pivonia.middleware.MissingMiddlewareException

import java.time.Instant

class TimeLayer implements Middleware<TimeMessage> {

    @Override
    void initialize(MiddlewareClient<TimeMessage> middlewareClient) throws MissingMiddlewareException {
        // nothing to do here
    }

    @Override
    Optional<TimeMessage> handleIncomingMessage(TimeMessage message) {
        return Optional.of(message)
    }

    @Override
    Optional<TimeMessage> handleOutgoingMessage(TimeMessage message) {
        def timeMessage = new TimeMessage(
                message.getRecipientId(),
                message.getSenderId(),
                Instant.now().toEpochMilli()
        )
        return Optional.of(timeMessage)
    }

    @Override
    void close() throws Exception {
        // nothing to do here
    }
}