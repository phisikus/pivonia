package eu.phisikus.pivonia.pool.mediators

import eu.phisikus.pivonia.pool.AddressPool
import eu.phisikus.pivonia.pool.ClientPool
import eu.phisikus.pivonia.pool.HeartbeatPool
import io.reactivex.subjects.PublishSubject
import spock.lang.Specification

class ConnectionManagerImplSpec extends Specification {

    def "Should return encapsulated pools and dispose resources properly"() {
        given: "Client, Address, Heartbeat pools are defined"
        def clientPool = Mock(ClientPool)
        def addressPool = Mock(AddressPool)
        def heartbeatPool = Mock(HeartbeatPool)

        and: "event streams are monitored"
        1 * clientPool.getClientChanges() >> PublishSubject.create()
        1 * addressPool.getAddressChanges() >> PublishSubject.create()
        1 * heartbeatPool.getHeartbeatChanges() >> PublishSubject.create()

        and: "connection manager is created"
        def manager = new ConnectionManagerImpl(clientPool, addressPool, heartbeatPool, null, 1)

        expect: "correct instances to be returned by getters"
        manager.getClientPool() == clientPool
        manager.getAddressPool() == addressPool
        manager.getHeartbeatPool() == heartbeatPool

        and: "manager to be disposed properly when requested"
        manager.dispose()
        manager.isDisposed()
    }
}
