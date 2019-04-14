package eu.phisikus.pivonia.pool.mediators

import eu.phisikus.pivonia.pool.AddressPool
import eu.phisikus.pivonia.pool.HeartbeatPool
import eu.phisikus.pivonia.pool.TransmitterPool
import io.reactivex.subjects.PublishSubject
import spock.lang.Specification

class ConnectionManagerImplSpec extends Specification {

    def "Should return encapsulated pools and dispose resources properly"() {
        given: "Transmitter, Address and Heartbeat pools are defined"
        def transmitterPool = Mock(TransmitterPool)
        def addressPool = Mock(AddressPool)
        def heartbeatPool = Mock(HeartbeatPool)

        and: "event streams are monitored"
        1 * transmitterPool.getChanges() >> PublishSubject.create()
        1 * addressPool.getChanges() >> PublishSubject.create()
        1 * heartbeatPool.getHeartbeatChanges() >> PublishSubject.create()

        and: "connection manager is created"
        def manager = new ConnectionManagerImpl(transmitterPool, addressPool, heartbeatPool, null, 1)

        expect: "correct instances to be returned by getters"
        manager.getTransmitterPool() == transmitterPool
        manager.getAddressPool() == addressPool
        manager.getHeartbeatPool() == heartbeatPool

        and: "manager to be disposed properly when requested"
        manager.dispose()
        manager.isDisposed()
    }
}
