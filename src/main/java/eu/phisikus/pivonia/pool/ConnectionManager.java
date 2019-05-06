package eu.phisikus.pivonia.pool;

import io.reactivex.disposables.Disposable;

/**
 * Encapsulates configured Server, Transmitter, Address and Heartbeat Pools.
 * Every address added or removed from the pool will connect or disconnect clients in the transmitter pool.
 * Every client added or removed from the pool will be added or removed from the heartbeat pool.
 * In general once you add an address, connection will be made with that node and monitored with heartbeat.
 * Incoming connections to the server pool will be treated as transmitter instances and added to the flow.
 *
 * @param <K> type of node ID
 */
public interface ConnectionManager<K> extends Disposable {
    TransmitterPool<K> getTransmitterPool();

    AddressPool getAddressPool();

    ClientHeartbeatPool<K> getClientHeartbeatPool();

    ServerHeartbeatPool<K> getServerHeartbeatPool();

    ServerPool getServerPool();
}
