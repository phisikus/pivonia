package eu.phisikus.pivonia.api;

import eu.phisikus.pivonia.middleware.MissingMiddlewareException;
import eu.phisikus.pivonia.middleware.StateContainer;

/**
 * Middleware represents a layer of algorithm in message processing.
 * Initialization phase allows for the layer to get some information from common state container.
 * Two MessageProcessors have to be defined - one for client and one for server-side message handling.
 *
 * @param <T> type of message used for data transfer
 */
public interface Middleware<T> {

    /**
     * It initializes middleware. At this stage the middleware can register some data structure in the container.
     * If it depends on data structure from another middleware it can be detected here and exception can be thrown.
     *
     * @param stateContainer state container for storing shared data
     * @throws MissingMiddlewareException exception thrown if some required layer is missing
     */
    void initialize(StateContainer stateContainer) throws MissingMiddlewareException;

    /**
     * Returns a MessageProcessor that will be called for messages received by the client
     *
     * @return message processor on the client side
     */
    MessageProcessor<T> getClientSideMessageProcessor();


    /**
     * Returns a MessageProcessor that will be called for messages received by the server
     *
     * @return message processor on the server side
     */
    MessageProcessor<T> getServerSideMessageProcessor();
}
