package eu.phisikus.pivonia.pool.mediators

import eu.phisikus.pivonia.pool.*
import io.reactivex.subjects.PublishSubject
import spock.lang.Specification

class ConnectionManagerImplSpec extends Specification {

    def "Should return encapsulated pools from connection manager"() {
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

        when: "connection manager is created"
        def manager = new ConnectionManagerImpl(
                transmitterPool,
                addressPool,
                clientHeartbeatPool,
                serverHeartbeatPool,
                serverPool,
                null,
                1
        )

        then: "correct instances are returned by getters"
        manager.getTransmitterPool() == transmitterPool
        manager.getAddressPool() == addressPool
        manager.getClientHeartbeatPool() == clientHeartbeatPool
        manager.getServerHeartbeatPool() == serverHeartbeatPool
        manager.getServerPool() == serverPool
    }

    def "Should dispose of encapsulated pools properly"() {
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

        and: "expected disposal calls are defined"
        1 * transmitterPool.dispose()
        1 * serverPool.dispose()
        1 * clientHeartbeatPool.dispose()
        1 * serverHeartbeatPool.dispose()

        and: "positive disposal results as well"
        1 * transmitterPool.isDisposed() >> true
        1 * serverPool.isDisposed() >> true
        1 * clientHeartbeatPool.isDisposed() >> true
        1 * serverHeartbeatPool.isDisposed() >> true

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

        when: "manager disposal is performed"
        manager.dispose()

        then: "manager is marked as disposed"
        manager.isDisposed()

    }

}
