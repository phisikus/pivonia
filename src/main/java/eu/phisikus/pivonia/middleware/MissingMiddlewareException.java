package eu.phisikus.pivonia.middleware;

/**
 * Used by the Middleware to indicate that some type of dependency is missing.
 */
public class MissingMiddlewareException extends RuntimeException {
    public MissingMiddlewareException(Class typeOfMiddleware) {
        super("Missing middleware component of type: " + typeOfMiddleware.getCanonicalName());
    }
}
