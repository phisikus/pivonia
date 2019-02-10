package eu.phisikus.pivonia.pool;

import io.reactivex.disposables.Disposable;

/**
 * Encapsulates configured Client, Address and Heartbeat Pools.
 * Every address added or removed from the pool will connect or disconnect clients in the client pool.
 * Every client added or removed from the pool will be added or removed from the heartbeat pool.
 * In general once you add an address, connection will be made with that node and monitored with heartbeat.
 *
 * @param <K> type of node ID
 */
public interface ConnectionManager<K> extends Disposable {
    ClientPool<K> getClientPool();
    AddressPool getAddressPool();
    HeartbeatPool<K> getHeartbeatPool();
}
