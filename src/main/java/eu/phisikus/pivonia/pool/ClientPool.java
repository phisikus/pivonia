package eu.phisikus.pivonia.pool;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.pool.client.ClientEvent;
import io.reactivex.Observable;

import java.util.List;
import java.util.Optional;

/**
 * Represents resource pool containing clients with assigned node ID.
 * The idea is to associate connected client with node of given ID.
 *
 * @param <K> type of node ID
 */
public interface ClientPool<K> {

    /**
     * Get all clients from the pool.
     *
     * @return all clients in the pool
     */
    List<Client> getClients();

    /**
     * Get client associated with given node ID.
     *
     * @param id identifier of node
     * @return client connected to node using provided ID
     */
    Optional<Client> get(K id);

    /**
     * Associate client with given ID.
     * The client should be already a part of the pool before assignment.
     *
     * @param id node id
     * @param client client instance that will be associated with new id
     */
    void set(K id, Client client);


    /**
     * Add client to the pool.
     * One client can be associated with multiple node IDs
     *
     * @param client client that will be added to the pool
     */
    void add(Client client);


    /**
     * Remove client from the pool.
     * All node IDs associations with this client will be removed.
     *
     * @param client client that will be removed from the pool
     */
    void remove(Client client);


    /**
     * Observable source of client change events.
     * Every add/delete operation triggers change event.
     *
     * @return observable client changes
     */
    Observable<ClientEvent> getClientChanges();
}
