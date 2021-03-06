package eu.phisikus.pivonia.node;

import eu.phisikus.pivonia.logic.MessageHandlers;
import io.reactivex.disposables.Disposable;

/**
 * Middleware extends Node with new capabilities.
 * It allows Node to register needed MessageHandlers and call required initializing actions.
 *
 * @param <K> type of node ID
 * @param <S> type of state object
 */
public interface Middleware<K, S> extends Disposable {

    /**
     * Provides message handlers important to the algorithm implemented by the Middleware
     *
     * @return algorithm specific MessageHandlers
     */
    MessageHandlers<Node<K, S>> getMessageHandlers();

    /**
     * Initializes Middleware.
     * Provided Node instance can be used to retrieve needed dependencies and initialize the algorithm.
     *
     * @param node initialized Node instance
     */
    void init(Node<K, S> node);
}
