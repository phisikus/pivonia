package eu.phisikus.pivonia.pool;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.pool.heartbeat.events.HeartbeatPoolEvent;
import io.reactivex.Observable;

/**
 * Each client added to the pool will be asked to send greeting messages to connected nodes.
 * Once the response is received and node ID is returned an event will be emitted.
 * This process will be performed periodically to keep information about clients and nodes that they are connected to.
 *
 * @param <K> type of node ID
 */
public interface ClientHeartbeatPool<K> {

    /**
     * Add client to heartbeat pool.
     * Additional message observer will be added as a side effect.
     * Messages will be sent through given client periodically.
     *
     * @param client client that will be used to send messages
     */
    void add(Client client);


    /**
     * Remove client from heartbeat pool.
     *
     * @param client client that will be removed from the pool
     */
    void remove(Client client);


    /**
     * Observable source of heartbeat events.
     * Every time some client returns response or timeout condition happens, event is emitted.
     *
     * @return observable heartbeat events
     */
    Observable<HeartbeatPoolEvent> getHeartbeatChanges();
}
