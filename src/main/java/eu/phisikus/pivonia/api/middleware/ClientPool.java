package eu.phisikus.pivonia.api.middleware;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.MessageHandler;
import eu.phisikus.pivonia.api.Server;

import java.util.Optional;
import java.util.function.Function;

/**
 * This ClientPool manages a list of connected Clients.
 * The assumption is that a certain node identifies by some key of type K.
 * If that node can be accessed through one of the clients, the pool will provide that Client.
 *
 * @param <K> type of key used to identify nodes
 * @param <T> type of message used in communication
 */
public interface ClientPool<K, T> extends AutoCloseable {
    /**
     * It provides an instance of Client connected to node identifying with certain ID.
     *
     * @param id key that the connected node is identifying with (ID)
     * @return if found the object will contain an instance of connected Client.
     */
    Optional<Client> get(K id);

    /**
     * Checks if there is a Client connected to a node that uses certain ID.
     *
     * @param id key used by the requested node
     * @return true if there is an active Client
     */
    boolean exists(K id);


    /**
     * Creates a mapping between ID and Client instance
     *
     * @param id     ID of the node connected to the client
     * @param client instance of the Client with node using mentioned ID
     */
    void set(K id, Client client);

    /**
     * Removes client from any mappings (for example if it is considered dead).
     *
     * @param client Client instance that should be removed from the pool
     */
    void remove(Client client);


    /**
     * Adds a new client to the pool by calling provided builder.
     * The message handler passed as argument should be used to allow for binding the Client with infrastructure.
     *
     * @param clientBuilder function that creates Client instance and uses provided MessageHandler to do so
     */
    void addUsingBuilder(Function<MessageHandler<T>, Client> clientBuilder);


    /**
     * This function uses provided server builder to create the server with custom MessageHandler.
     * That handler will intercept any incoming messages and register new Client instances into the pool.
     *
     * @param serverBuilder function that creates the Server instance and uses provided MessageHandler to do so
     */
    void addSourceUsingBuilder(Function<MessageHandler<T>, Server> serverBuilder);
}

