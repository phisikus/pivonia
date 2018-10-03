package eu.phisikus.pivonia.pool;

import eu.phisikus.pivonia.api.Client;

import java.util.Optional;

/**
 * This ClientPool manages a list of connected Clients.
 * The assumption is that a certain node identifies by some key of type K.
 * If that node can be accessed through one of the clients, the pool will provide that Client.
 *
 * @param <K> type of key used to identify nodes
 */
public interface ClientPool<K> extends AutoCloseable {
    /**
     * It provides an instance of Client connected to node identifying with certain ID.
     *
     * @param id key that the connected node is identifying with (ID)
     * @return if found the object will contain an instance of connected Client.
     */
    Optional<Client> getClient(K id);

    /**
     * Checks if there is a Client connected to a node that uses certain ID.
     *
     * @param id key used by the requested node
     * @return true if there is an active Client
     */
    boolean isAvailable(K id);
}
