package eu.phisikus.pivonia.pool.mediators

import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.pool.ClientHeartbeatPool
import eu.phisikus.pivonia.pool.TransmitterPool
import eu.phisikus.pivonia.pool.heartbeat.events.ReceivedEvent
import eu.phisikus.pivonia.pool.heartbeat.events.TimeoutEvent
import eu.phisikus.pivonia.pool.transmitter.events.AdditionEvent
import eu.phisikus.pivonia.pool.transmitter.events.RemovalEvent
import io.reactivex.subjects.PublishSubject
import spock.lang.Specification

class ClientHeartbeatPoolMediatorSpec extends Specification {

    def "Should add new client to the ClientHeartbeatPool on event from TransmitterPool"() {
        given: "TransmitterPool and ClientHeartbeatPool are defined"
        final transmitterPool = Mock(TransmitterPool)
        final heartbeatPool = Mock(ClientHeartbeatPool)
        heartbeatPool.getHeartbeatChanges() >> PublishSubject.create()

        and: "Transmitter addition event is defined"
        final client = Mock(Client)
        final additionEvent = new AdditionEvent(client)

        and: "TransmitterPool is configured to publish change events"
        final clientChanges = PublishSubject.create()
        transmitterPool.getChanges() >> clientChanges

        and: "mediator is created between TransmitterPool and ClientHeartbeatPool"
        final mediator = new ClientHeartbeatPoolMediator(transmitterPool, heartbeatPool)

        when: "new Client is added to the TransmitterPool"
        clientChanges.onNext(additionEvent)

        then: "Client is added to the ClientHeartbeatPool"
        1 * heartbeatPool.add(client)

        cleanup: "mediator is destroyed"
        mediator.dispose()
    }

    def "Should remove client from the ClientHeartbeatPool on event from TransmitterPool"() {
        given: "TransmitterPool and ClientHeartbeatPool are defined"
        final transmitterPool = Mock(TransmitterPool)
        final heartbeatPool = Mock(ClientHeartbeatPool)
        heartbeatPool.getHeartbeatChanges() >> PublishSubject.create()

        and: "Client removal event is defined"
        final client = Mock(Client)
        final removalEvent = new RemovalEvent(client)

        and: "TransmitterPool is configured to publish change events"
        final transmitterPoolEvents = PublishSubject.create()
        transmitterPool.getChanges() >> transmitterPoolEvents

        and: "mediator is created between TransmitterPool and ClientHeartbeatPool"
        final mediator = new ClientHeartbeatPoolMediator(transmitterPool, heartbeatPool)

        when: "Client is removed from the TransmitterPool"
        transmitterPoolEvents.onNext(removalEvent)

        then: "Client is removed from the ClientHeartbeatPool"
        1 * heartbeatPool.remove(client)

        cleanup: "mediator is destroyed"
        mediator.dispose()
    }

    def "Should assign client to node ID on heartbeat response retrieval"() {
        given: "TransmitterPool and ClientHeartbeatPool are defined"
        final transmitterPool = Mock(TransmitterPool)
        final heartbeatPool = Mock(ClientHeartbeatPool)
        transmitterPool.getChanges() >> PublishSubject.create()

        and: "Heartbeat response event is defined"
        final client = Mock(Client)
        final nodeId = UUID.randomUUID()
        final heartbeatEvent = new ReceivedEvent<UUID>(nodeId, client)

        and: "HeartbeatPool is configured to publish change events"
        final heartbeatPoolEvents = PublishSubject.create()
        heartbeatPool.getHeartbeatChanges() >> heartbeatPoolEvents

        and: "mediator is created between TransmitterPool and ClientHeartbeatPool"
        final mediator = new ClientHeartbeatPoolMediator(transmitterPool, heartbeatPool)

        when: "heartbeat response was received by the HeartbeatPool"
        heartbeatPoolEvents.onNext(heartbeatEvent)

        then: "assignment between node ID and Client is set in the TransmitterPool"
        1 * transmitterPool.set(nodeId, client)

        cleanup: "mediator is destroyed"
        mediator.dispose()
    }

    def "Should remove client from the TransmitterPool on heartbeat timeout"() {
        given: "TransmitterPool and ClientHeartbeatPool are defined"
        final transmitterPool = Mock(TransmitterPool)
        final heartbeatPool = Mock(ClientHeartbeatPool)
        transmitterPool.getChanges() >> PublishSubject.create()

        and: "Heartbeat timeout event is defined"
        final client = Mock(Client)
        final heartbeatEvent = new TimeoutEvent(client)

        and: "ClientHeartbeatPool is configured to publish change events"
        final heartbeatPoolEvents = PublishSubject.create()
        heartbeatPool.getHeartbeatChanges() >> heartbeatPoolEvents

        and: "mediator is created between TransmitterPool and ClientHeartbeatPool"
        final mediator = new ClientHeartbeatPoolMediator(transmitterPool, heartbeatPool)

        when: "heartbeat timeout occurred"
        heartbeatPoolEvents.onNext(heartbeatEvent)

        then: "Client is closed"
        1 * client.close()

        and: "removed from the TransmitterPool"
        1 * transmitterPool.remove(client)

        cleanup: "mediator is destroyed"
        mediator.dispose()
    }
}
