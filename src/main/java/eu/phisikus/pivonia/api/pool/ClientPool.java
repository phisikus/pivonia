package eu.phisikus.pivonia.api.pool;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.MessageHandler;
import eu.phisikus.pivonia.api.Server;
import io.reactivex.Observable;
import io.vavr.control.Try;

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
public interface ClientPool<K, T extends Envelope<K>> extends AutoCloseable {
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
     * Adds a new client to the pool by calling provided builder.
     * The message handler passed as argument should be used to allow for binding the Client with infrastructure.
     * Returned client should be already connected.
     *
     * @param clientBuilder function that creates Client instance and uses provided MessageHandler to do so
     * @return created client instance of failure
     */
    Try<Client> addUsingBuilder(Function<MessageHandler<T>, Try<Client>> clientBuilder);

    /**
     * This function uses provided server builder to create the server with custom MessageHandler.
     * That handler will intercept any incoming messages and register new Client instances into the pool.
     *
     * @param serverBuilder function that creates the Server instance and uses provided MessageHandler to do so
     * @return created server instance or failure
     */
    Try<Server> addSourceUsingBuilder(Function<MessageHandler<T>, Try<Server>> serverBuilder);

    /**
     * Associates client with given node ID.
     *
     * @param id     identifier od node that can be reached with given client
     * @param client connected client that can be used to communicate with node of given ID
     */
    void set(K id, Client client);


    /**
     * Removes (presumably broken) client from the client pool.
     *
     * @param client instance to remove
     */
    void remove(Client client);

    /**
     * Returns observable stream of messages coming from clients in the pool.
     *
     * @return observable client messages
     */
    Observable<MessageWithClient<T>> getClientMessages();

    /**
     * Returns observable stream of messages received by servers in the pool.
     *
     * @return observable server messages
     */
    Observable<MessageWithClient<T>> getServerMessages();

}

