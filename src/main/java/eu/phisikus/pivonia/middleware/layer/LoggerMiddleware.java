package eu.phisikus.pivonia.middleware.layer;

import eu.phisikus.pivonia.api.middleware.Middleware;
import eu.phisikus.pivonia.api.middleware.MiddlewareClient;
import eu.phisikus.pivonia.middleware.MissingMiddlewareException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * Middleware that uses provided logger to save information about incoming and outgoing messages.
 *
 * @param <T> type of message used in the system
 */
public class LoggerMiddleware<T> implements Middleware<T> {

    private Level logLevel;
    private Logger logger;

    public LoggerMiddleware(Logger logger, Level logLevel) {
        this.logLevel = logLevel;
        this.logger = logger;
    }

    @Override
    public void initialize(MiddlewareClient<T> middlewareClient) throws MissingMiddlewareException {
        logger.log(logLevel, "Initializing LoggerMiddleware layer.");
    }

    @Override
    public Optional<T> handleIncomingMessage(T message) {
        logger.log(logLevel, "Incoming message: " + message);
        return Optional.of(message);
    }

    @Override
    public Optional<T> handleOutgoingMessage(T message) {
        logger.log(logLevel, "Outgoing message: " + message);
        return Optional.empty();
    }

    @Override
    public void close() throws Exception {
        logger.log(logLevel, "Shutting down LoggerMiddleware.");
    }
}
