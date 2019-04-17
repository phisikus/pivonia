package eu.phisikus.pivonia.pool.mediators

import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.pool.HeartbeatPool
import eu.phisikus.pivonia.pool.TransmitterPool
import eu.phisikus.pivonia.pool.heartbeat.events.ReceivedEvent
import eu.phisikus.pivonia.pool.heartbeat.events.TimeoutEvent
import eu.phisikus.pivonia.pool.transmitter.events.AdditionEvent
import eu.phisikus.pivonia.pool.transmitter.events.RemovalEvent
import io.reactivex.subjects.PublishSubject
import spock.lang.Specification

class ClientHeartbeatPoolMediatorSpec extends Specification {

    def "Should add new client to the heartbeat pool on transmitter pool event"() {
        given: "Transmitter Pool and Heartbeat Pool are defined"
        final transmitterPool = Mock(TransmitterPool)
        final heartbeatPool = Mock(HeartbeatPool)
        heartbeatPool.getHeartbeatChanges() >> PublishSubject.create()

        and: "Transmitter addition event is defined"
        final client = Mock(Client)
        final additionEvent = new AdditionEvent(client)

        and: "Transmitter Pool is configured to publish change events"
        final clientChanges = PublishSubject.create()
        transmitterPool.getChanges() >> clientChanges

        and: "mediator is created between Transmitter Pool and Heartbeat Pool"
        final mediator = new ClientHeartbeatPoolMediator(transmitterPool, heartbeatPool)

        when: "new Client is added to the Transmitter Pool"
        clientChanges.onNext(additionEvent)

        then: "Client is added to the Heartbeat Pool"
        1 * heartbeatPool.add(client)

        cleanup: "mediator is destroyed"
        mediator.dispose()
    }

    def "Should remove client from the heartbeat pool on transmitter Pool event"() {
        given: "TransmitterPool and HeartbeatPool are defined"
        final transmitterPool = Mock(TransmitterPool)
        final heartbeatPool = Mock(HeartbeatPool)
        heartbeatPool.getHeartbeatChanges() >> PublishSubject.create()

        and: "Client removal event is defined"
        final client = Mock(Client)
        final removalEvent = new RemovalEvent(client)

        and: "Transmitter Pool is configured to publish change events"
        final transmitterPoolEvents = PublishSubject.create()
        transmitterPool.getChanges() >> transmitterPoolEvents

        and: "mediator is created between Transmitter Pool and Heartbeat Pool"
        final mediator = new ClientHeartbeatPoolMediator(transmitterPool, heartbeatPool)

        when: "Client is removed from the Transmitter Pool"
        transmitterPoolEvents.onNext(removalEvent)

        then: "Client is removed from the Heartbeat Pool"
        1 * heartbeatPool.remove(client)

        cleanup: "mediator is destroyed"
        mediator.dispose()
    }

    def "Should assign client to node ID on heartbeat response retrieval"() {
        given: "TransmitterPool and HeartbeatPool are defined"
        final transmitterPool = Mock(TransmitterPool)
        final heartbeatPool = Mock(HeartbeatPool)
        transmitterPool.getChanges() >> PublishSubject.create()

        and: "Heartbeat response event is defined"
        final client = Mock(Client)
        final nodeId = UUID.randomUUID()
        final heartbeatEvent = new ReceivedEvent<UUID>(nodeId, client)

        and: "Heartbeat Pool is configured to publish change events"
        final heartbeatPoolEvents = PublishSubject.create()
        heartbeatPool.getHeartbeatChanges() >> heartbeatPoolEvents

        and: "mediator is created between Transmitter Pool and Heartbeat Pool"
        final mediator = new ClientHeartbeatPoolMediator(transmitterPool, heartbeatPool)

        when: "heartbeat response was received by the Heartbeat Pool"
        heartbeatPoolEvents.onNext(heartbeatEvent)

        then: "assignment between node ID and Client is set in the Transmitter Pool"
        1 * transmitterPool.set(nodeId, client)

        cleanup: "mediator is destroyed"
        mediator.dispose()
    }

    def "Should remove client from the Transmitter Pool on heartbeat timeout"() {
        given: "TransmitterPool and HeartbeatPool are defined"
        final transmitterPool = Mock(TransmitterPool)
        final heartbeatPool = Mock(HeartbeatPool)
        transmitterPool.getChanges() >> PublishSubject.create()

        and: "Heartbeat timeout event is defined"
        final client = Mock(Client)
        final heartbeatEvent = new TimeoutEvent(client)

        and: "Heartbeat Pool is configured to publish change events"
        final heartbeatPoolEvents = PublishSubject.create()
        heartbeatPool.getHeartbeatChanges() >> heartbeatPoolEvents

        and: "mediator is created between Transmitter Pool and Heartbeat Pool"
        final mediator = new ClientHeartbeatPoolMediator(transmitterPool, heartbeatPool)

        when: "heartbeat timeout occurred"
        heartbeatPoolEvents.onNext(heartbeatEvent)

        then: "client is closed"
        1 * client.close()

        and: "removed from the Transmitter Pool"
        1 * transmitterPool.remove(client)

        cleanup: "mediator is destroyed"
        mediator.dispose()
    }
}
