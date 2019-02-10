package eu.phisikus.pivonia.pool.mediators

import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.pool.ClientPool
import eu.phisikus.pivonia.pool.HeartbeatPool
import eu.phisikus.pivonia.pool.client.ClientEvent
import eu.phisikus.pivonia.pool.heartbeat.HeartbeatEvent
import io.reactivex.subjects.PublishSubject
import spock.lang.Specification

class ClientHeartbeatPoolMediatorSpec extends Specification {

    def "Should add new client to the heartbeat pool on client pool event"() {
        given: "ClientPool and HeartbeatPool are defined"
        final clientPool = Mock(ClientPool)
        final heartbeatPool = Mock(HeartbeatPool)
        heartbeatPool.getHeartbeatChanges() >> PublishSubject.create()

        and: "Client addition event is defined"
        final client = Mock(Client)
        final additionEvent = new ClientEvent<>(client, null, ClientEvent.Operation.ADD)

        and: "Client Pool is configured to publish change events"
        final clientChanges = PublishSubject.create()
        clientPool.getClientChanges() >> clientChanges

        and: "mediator is created between Client Pool and Heartbeat Pool"
        final mediator = new ClientHeartbeatPoolMediator(clientPool, heartbeatPool)

        when: "new Client is added to the Client Pool"
        clientChanges.onNext(additionEvent)

        then: "Client is added to the Heartbeat Pool"
        1 * heartbeatPool.add(client)

        cleanup: "mediator is destroyed"
        mediator.dispose()
    }

    def "Should remove client from the heartbeat pool on client pool event"() {
        given: "ClientPool and HeartbeatPool are defined"
        final clientPool = Mock(ClientPool)
        final heartbeatPool = Mock(HeartbeatPool)
        heartbeatPool.getHeartbeatChanges() >> PublishSubject.create()

        and: "Client removal event is defined"
        final client = Mock(Client)
        final removalEvent = new ClientEvent<>(client, null, ClientEvent.Operation.REMOVE)

        and: "Client Pool is configured to publish change events"
        final clientChanges = PublishSubject.create()
        clientPool.getClientChanges() >> clientChanges

        and: "mediator is created between Client Pool and Heartbeat Pool"
        final mediator = new ClientHeartbeatPoolMediator(clientPool, heartbeatPool)

        when: "Client is removed from the Client Pool"
        clientChanges.onNext(removalEvent)

        then: "Client is removed from the Heartbeat Pool"
        1 * heartbeatPool.remove(client)

        cleanup: "mediator is destroyed"
        mediator.dispose()
    }

    def "Should assign client to node ID on heartbeat response retrieval"() {
        given: "ClientPool and HeartbeatPool are defined"
        final clientPool = Mock(ClientPool)
        final heartbeatPool = Mock(HeartbeatPool)
        clientPool.getClientChanges() >> PublishSubject.create()

        and: "Heartbeat response event is defined"
        final client = Mock(Client)
        final nodeId = UUID.randomUUID()
        final heartbeatEvent = new HeartbeatEvent<>(nodeId, client, HeartbeatEvent.Operation.RECEIVED)

        and: "Heartbeat Pool is configured to publish change events"
        final heartbeatChanges = PublishSubject.create()
        heartbeatPool.getHeartbeatChanges() >> heartbeatChanges

        and: "mediator is created between Client Pool and Heartbeat Pool"
        final mediator = new ClientHeartbeatPoolMediator(clientPool, heartbeatPool)

        when: "heartbeat response was received by the Heartbeat Pool"
        heartbeatChanges.onNext(heartbeatEvent)

        then: "assignment between node ID and Client is set in the Client Pool"
        1 * clientPool.set(nodeId, client)

        cleanup: "mediator is destroyed"
        mediator.dispose()
    }

    def "Should remove client from the Client Pool on heartbeat timeout"() {
        given: "ClientPool and HeartbeatPool are defined"
        final clientPool = Mock(ClientPool)
        final heartbeatPool = Mock(HeartbeatPool)
        clientPool.getClientChanges() >> PublishSubject.create()

        and: "Heartbeat timeout event is defined"
        final client = Mock(Client)
        final nodeId = UUID.randomUUID()
        final heartbeatEvent = new HeartbeatEvent<>(nodeId, client, HeartbeatEvent.Operation.TIMEOUT)

        and: "Heartbeat Pool is configured to publish change events"
        final heartbeatChanges = PublishSubject.create()
        heartbeatPool.getHeartbeatChanges() >> heartbeatChanges

        and: "mediator is created between Client Pool and Heartbeat Pool"
        final mediator = new ClientHeartbeatPoolMediator(clientPool, heartbeatPool)

        when: "heartbeat timeout occurred"
        heartbeatChanges.onNext(heartbeatEvent)

        then: "client is closed"
        1 * client.close()

        and: "removed from the Client Pool"
        1 * clientPool.remove(client)

        cleanup: "mediator is destroyed"
        mediator.dispose()
    }
}
