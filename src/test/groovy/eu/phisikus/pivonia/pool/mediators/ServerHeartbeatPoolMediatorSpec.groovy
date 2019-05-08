package eu.phisikus.pivonia.pool.mediators

import eu.phisikus.pivonia.api.Server
import eu.phisikus.pivonia.api.Transmitter
import eu.phisikus.pivonia.pool.ServerHeartbeatPool
import eu.phisikus.pivonia.pool.ServerPool
import eu.phisikus.pivonia.pool.TransmitterPool
import eu.phisikus.pivonia.pool.heartbeat.events.ReceivedEvent
import eu.phisikus.pivonia.pool.heartbeat.events.TimeoutEvent
import eu.phisikus.pivonia.pool.server.ServerPoolEvent
import io.reactivex.subjects.PublishSubject
import spock.lang.Specification

class ServerHeartbeatPoolMediatorSpec extends Specification {
    /**
     * TODO
     * 1. Check IT tests - remove visitor usage? check if needed
     * 2. Use the server heartbeat pool in IT tests - prove that it works?
     * 3. Unify naming of classes in javadocs & test step comments
     */

    def "Should add server to heartbeat pool once it is added in the server pool"() {
        given: "there is a server"
        def server = Mock(Server)

        and: "server and heartbeat pools"
        def serverPool = Mock(ServerPool)
        def serverHeartbeatPool = Mock(ServerHeartbeatPool)
        def transmitterPool = Mock(TransmitterPool)
        def serverEvents = PublishSubject.create()
        def heartbeatEvents = PublishSubject.create()
        1 * serverPool.getChanges() >> serverEvents
        1 * serverHeartbeatPool.getHeartbeatChanges() >> heartbeatEvents

        and: "mediator is created"
        def mediator = new ServerHeartbeatPoolMediator<>(serverPool, transmitterPool, serverHeartbeatPool)

        when: "adding to the server pool"
        def additionEvent = new ServerPoolEvent(server, ServerPoolEvent.Operation.ADD)
        serverEvents.onNext(additionEvent)

        then: "it is also added to the heartbeat pool"
        1 * serverHeartbeatPool.add(server)

        cleanup: "mediator is disposed"
        mediator.dispose()
    }

    def "Should remove server to heartbeat pool once it is deleted from the server pool"() {
        given: "there is a server"
        def server = Mock(Server)

        and: "server and heartbeat pools"
        def serverPool = Mock(ServerPool)
        def serverHeartbeatPool = Mock(ServerHeartbeatPool)
        def transmitterPool = Mock(TransmitterPool)
        def serverEvents = PublishSubject.create()
        def heartbeatEvents = PublishSubject.create()
        1 * serverPool.getChanges() >> serverEvents
        1 * serverHeartbeatPool.getHeartbeatChanges() >> heartbeatEvents

        and: "mediator is created"
        def mediator = new ServerHeartbeatPoolMediator<>(serverPool, transmitterPool, serverHeartbeatPool)

        when: "removing from the server pool"
        def removalEvent = new ServerPoolEvent(server, ServerPoolEvent.Operation.REMOVE)
        serverEvents.onNext(removalEvent)

        then: "it is also removed from the heartbeat pool"
        1 * serverHeartbeatPool.remove(server)

        cleanup: "mediator is disposed"
        mediator.dispose()
    }

    def "Should assign transmitter to ID on heartbeat reception"() {
        given: "there is a transmitter"
        def transmitter = Mock(Transmitter)
        def nodeId = "node0"

        and: "transmitter and heartbeat pools"
        def serverPool = Mock(ServerPool)
        def serverHeartbeatPool = Mock(ServerHeartbeatPool)
        def transmitterPool = Mock(TransmitterPool)
        def heartbeatEvents = PublishSubject.create()
        def serverEvents = PublishSubject.create()
        1 * serverPool.getChanges() >> serverEvents
        1 * serverHeartbeatPool.getHeartbeatChanges() >> heartbeatEvents

        and: "mediator is created"
        def mediator = new ServerHeartbeatPoolMediator<>(serverPool, transmitterPool, serverHeartbeatPool)

        when: "heartbeat is received"
        heartbeatEvents.onNext(new ReceivedEvent(nodeId, transmitter))

        then: "assignment is made in the transmitter pool"
        1 * transmitterPool.set(nodeId, transmitter)

        cleanup: "mediator is disposed"
        mediator.dispose()
    }

    def "Should remove transmitter from the pool on heartbeat timeout"() {
        given: "there is a transmitter"
        def transmitter = Mock(Transmitter)

        and: "transmitter and heartbeat pools"
        def serverPool = Mock(ServerPool)
        def serverHeartbeatPool = Mock(ServerHeartbeatPool)
        def transmitterPool = Mock(TransmitterPool)
        def heartbeatEvents = PublishSubject.create()
        def serverEvents = PublishSubject.create()
        1 * serverPool.getChanges() >> serverEvents
        1 * serverHeartbeatPool.getHeartbeatChanges() >> heartbeatEvents

        and: "mediator is created"
        def mediator = new ServerHeartbeatPoolMediator<>(serverPool, transmitterPool, serverHeartbeatPool)

        when: "timeout occurred"
        heartbeatEvents.onNext(new TimeoutEvent(transmitter))

        then: "transmitter pool is called to remove transmitter"
        1 * transmitterPool.remove(transmitter)

        cleanup: "mediator is disposed"
        mediator.dispose()
    }
}
