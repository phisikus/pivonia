package eu.phisikus.pivonia.pool;

import eu.phisikus.pivonia.api.Server;
import eu.phisikus.pivonia.pool.server.ServerPoolEvent;
import io.reactivex.Observable;

import java.util.List;

/**
 * Represents resource pool containing servers that represent entrypoints for a single node of application.
 */
public interface ServerPool {

    /**
     * Add running server to the Server Pool
     *
     * @param server instance of server that will be added to the pool
     */
    void add(Server server);


    /**
     * Remove server instance from the Server Pool
     *
     * @param server instance of server that will be remove from the pool
     */
    void remove(Server server);

    /**
     * Return list of all Servers that belong to this server pool
     *
     * @return list of Server instances
     */
    List<Server> getServers();

    /**
     * Observable source of change events for this server pool
     *
     * @return observable server pool changes
     */
    Observable<ServerPoolEvent> getChanges();
}
