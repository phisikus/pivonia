package eu.phisikus.pivonia.pool;

import eu.phisikus.pivonia.api.Transmitter;
import eu.phisikus.pivonia.pool.transmitter.TransmitterPoolEvent;
import io.reactivex.Observable;

import java.util.List;
import java.util.Optional;

/**
 * Represents resource pool containing transmitters with assigned node ID.
 * The idea is to associate connected client with node of given ID.
 *
 * @param <K> type of node ID
 */
public interface TransmitterPool<K> {

    /**
     * Get all transmitters from the pool.
     *
     * @return all transmitters in the pool
     */
    List<Transmitter> getTransmitters();

    /**
     * Get transmitter associated with given node ID.
     *
     * @param id identifier of node
     * @return transmitter connected to node using provided ID
     */
    Optional<Transmitter> get(K id);

    /**
     * Associate transmitter with given ID.
     * The transmitter should be already a part of the pool before assignment.
     *
     * @param id          node id
     * @param transmitter transmitter instance that will be associated with new id
     */
    void set(K id, Transmitter transmitter);


    /**
     * Add transmitter to the pool.
     * One transmitter can be associated with multiple node IDs
     *
     * @param transmitter transmitter that will be added to the pool
     */
    void add(Transmitter transmitter);


    /**
     * Remove transmitter from the pool.
     * All node IDs associations with this transmitter will be removed.
     *
     * @param transmitter transmitter that will be removed from the pool
     */
    void remove(Transmitter transmitter);


    /**
     * Observable source of transmitter change events.
     * Every add/delete operation triggers change event.
     *
     * @return observable pool changes
     */
    Observable<TransmitterPoolEvent> getChanges();
}
