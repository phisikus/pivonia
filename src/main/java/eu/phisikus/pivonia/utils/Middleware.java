package eu.phisikus.pivonia.utils;

import eu.phisikus.pivonia.logic.MessageHandlers;

/**
 * Middleware can be used to extend Node with new capabilities.
 * This interface allows Node to register needed MessageHandlers and call required initializing actions.
 *
 * @param <K> type of node ID
 * @param <S> type of state object
 */
public interface Middleware<K, S> {

    /**
     * Provides message handlers important to the algorithm implemented by the Middleware
     *
     * @return custom MessageHandlers
     */
    MessageHandlers<Node<K, S>> getMessageHandlers();

    /**
     * Initializes Middleware.
     * Function will be called once Node instance is initialized. That instance is passed as an argument.
     *
     * @param node Node instance
     */
    void init(Node<K, S> node);
}
