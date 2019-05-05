package eu.phisikus.pivonia.pool;

import eu.phisikus.pivonia.api.Server;
import eu.phisikus.pivonia.pool.heartbeat.events.HeartbeatPoolEvent;
import io.reactivex.Observable;

import java.util.List;

/**
 * Servers added to this pool gain the ability to participate in the heartbeat protocol.
 * Each added server will start to listen to heartbeat messages and respond correctly to the clients.
 *
 * @param <K> type of node ID
 */
public interface ServerHeartbeatPool<K> {
    /**
     * Adds server to the pool.
     * After addition server will start to participate in heartbeat protocol.
     *
     * @param server instance of server that will be added
     */
    void add(Server server);

    /**
     * Removes server from the pool.
     * After removal server will no longer participate in heartbeat protocol.
     *
     * @param server instance of server that will be removed
     */
    void remove(Server server);

    /**
     * Returns all server instances managed by the pool
     *
     * @return all servers that belong to the pool
     */
    List<Server> getServers();

    /**
     * Observable source of heartbeat events.
     * Every time some client returns response or timeout condition happens, event is emitted.
     *
     * @return observable heartbeat events
     */
    Observable<HeartbeatPoolEvent> getHeartbeatChanges();
}
