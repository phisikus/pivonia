package eu.phisikus.pivonia.pool.mediators

import eu.phisikus.pivonia.pool.AddressPool
import eu.phisikus.pivonia.pool.ClientHeartbeatPool
import eu.phisikus.pivonia.pool.ServerHeartbeatPool
import eu.phisikus.pivonia.pool.ServerPool
import eu.phisikus.pivonia.pool.TransmitterPool
import io.reactivex.subjects.PublishSubject
import spock.lang.Specification

class ConnectionManagerImplSpec extends Specification {

    def "Should return encapsulated pools and dispose resources properly"() {
        given: "Server, Transmitter, Address and Heartbeat pools are defined"
        def serverPool = Mock(ServerPool)
        def transmitterPool = Mock(TransmitterPool)
        def addressPool = Mock(AddressPool)
        def clientHeartbeatPool = Mock(ClientHeartbeatPool)
        def serverHeartbeatPool = Mock(ServerHeartbeatPool)

        and: "event streams are monitored"
        1 * transmitterPool.getChanges() >> PublishSubject.create()
        1 * addressPool.getChanges() >> PublishSubject.create()
        1 * serverPool.getChanges() >> PublishSubject.create()
        1 * clientHeartbeatPool.getHeartbeatChanges() >> PublishSubject.create()
        1 * serverHeartbeatPool.getHeartbeatChanges() >> PublishSubject.create()

        and: "connection manager is created"
        def manager = new ConnectionManagerImpl(
                transmitterPool,
                addressPool,
                clientHeartbeatPool,
                serverHeartbeatPool,
                serverPool,
                null,
                1
        )

        expect: "correct instances to be returned by getters"
        manager.getTransmitterPool() == transmitterPool
        manager.getAddressPool() == addressPool
        manager.getClientHeartbeatPool() == clientHeartbeatPool
        manager.getServerHeartbeatPool() == serverHeartbeatPool
        manager.getServerPool() == serverPool

        and: "manager to be disposed properly when requested"
        manager.dispose()
        manager.isDisposed()
    }
}
