package eu.phisikus.pivonia.pool.transmitter

import eu.phisikus.pivonia.api.Client
import io.reactivex.Observer
import spock.lang.Specification

class TransmitterPoolImplSpec extends Specification {

    def "Should add transmitter to the pool properly"() {
        given: "there is a transmitter"
        def transmitter = Mock(Client)

        and: "an empty pool"
        def transmitterPool = new TransmitterPoolImpl()

        and: "change events are monitored"
        def expectedEvent = new TransmitterPoolEvent<>(transmitter, null, TransmitterPoolEvent.Operation.ADD)
        def changes = transmitterPool.getChanges()
        def changeListener = Mock(Observer)
        changes.subscribe(changeListener)

        when: "adding the transmitter to the pool"
        transmitterPool.add(transmitter)

        then: "the pool contains that transmitter"
        transmitterPool.getTransmitters().contains(transmitter)

        and: "addition event was emitted"
        1 * changeListener.onNext(expectedEvent)
    }

    def "Should remove client from the pool properly"() {
        given: "there is a transmitter"
        def transmitter = Mock(Client)

        and: "transmitter pool with only that one transmitter"
        def transmitterPool = new TransmitterPoolImpl()
        transmitterPool.add(transmitter)

        and: "change events are monitored"
        def expectedEvent = new TransmitterPoolEvent<>(transmitter, null, TransmitterPoolEvent.Operation.REMOVE)
        def changes = transmitterPool.getChanges()
        def changeListener = Mock(Observer)
        changes.subscribe(changeListener)

        when: "removing the transmitter to the pool"
        transmitterPool.remove(transmitter)

        then: "the pool does not contain that transmitter anymore"
        !transmitterPool.getTransmitters().contains(transmitter)

        and: "deletion event was emitted"
        1 * changeListener.onNext(expectedEvent)
    }

    def "Should assign client to node ID"() {
        given: "there is a transmitter pool"
        def transmitterPool = new TransmitterPoolImpl()

        and: "it contains a transmitter"
        def nodeId = "first"
        def transmitter = Mock(Client)
        transmitterPool.add(transmitter)

        and: "change events are monitored"
        def changes = transmitterPool.getChanges()
        def changeListener = Mock(Observer)
        def expectedEvent = new TransmitterPoolEvent(transmitter, nodeId, TransmitterPoolEvent.Operation.ASSIGN)
        changes.subscribe(changeListener)

        when: "assigning transmitter with node ID"
        transmitterPool.set(nodeId, transmitter)

        then: "that transmitter can be retrieved using node ID"
        transmitterPool.get(nodeId).get() == transmitter

        and: "assignment event was emitted"
        1 * changeListener.onNext(expectedEvent)

    }

    def "Should unassign client on removal"() {
        given: "there is a transmitter pool"
        def transmitterPool = new TransmitterPoolImpl()

        and: "it contains a transmitter"
        def nodeId = "first"
        def transmitter = Mock(Client)
        transmitterPool.add(transmitter)

        and: "change events are monitored"
        def transmitterPoolEvents = transmitterPool.getChanges()
        def changeListener = Mock(Observer)
        def assignEvent = new TransmitterPoolEvent(transmitter, nodeId, TransmitterPoolEvent.Operation.ASSIGN)
        def unassignEvent = new TransmitterPoolEvent(transmitter, nodeId, TransmitterPoolEvent.Operation.UNASSIGN)
        def deleteEvent = new TransmitterPoolEvent(transmitter, null, TransmitterPoolEvent.Operation.REMOVE)
        transmitterPoolEvents.subscribe(changeListener)

        when: "assigning transmitter with node ID"
        transmitterPool.set(nodeId, transmitter)

        and: "removing it"
        transmitterPool.remove(transmitter)

        then: "that transmitter cannot be retrieved using node ID"
        !transmitterPool.get(nodeId).isPresent()

        and: "events were emitted"
        1 * changeListener.onNext(assignEvent)
        1 * changeListener.onNext(unassignEvent)
        1 * changeListener.onNext(deleteEvent)
    }

    def "Should assign new client to node ID"() {
        given: "there is a transmitter pool"
        def transmitterPool = new TransmitterPoolImpl()

        and: "it contains two transmitters"
        def nodeId = "node"
        def firstTransmitter = Mock(Client)
        def secondTransmitter = Mock(Client)
        transmitterPool.add(firstTransmitter)
        transmitterPool.add(secondTransmitter)

        and: "change events are monitored"
        def changes = transmitterPool.getChanges()
        def changeListener = Mock(Observer)
        def firstEvent = new TransmitterPoolEvent(firstTransmitter, nodeId, TransmitterPoolEvent.Operation.ASSIGN)
        def secondEvent = new TransmitterPoolEvent(firstTransmitter, nodeId, TransmitterPoolEvent.Operation.UNASSIGN)
        def thirdEvent = new TransmitterPoolEvent(secondTransmitter, nodeId, TransmitterPoolEvent.Operation.ASSIGN)
        changes.subscribe(changeListener)

        when: "assigning transmitter with node ID"
        transmitterPool.set(nodeId, firstTransmitter)

        and: "assigning second transmitter with the same ID"
        transmitterPool.set(nodeId, secondTransmitter)

        then: "second transmitter can be retrieved using node ID"
        transmitterPool.get(nodeId).get() == secondTransmitter

        and: "assignment events were emitted"
        1 * changeListener.onNext(firstEvent)
        1 * changeListener.onNext(secondEvent)
        1 * changeListener.onNext(thirdEvent)
    }


    def "Should update client to node ID assignment"() {
        given: "there is a transmitter pool"
        def transmitterPool = new TransmitterPoolImpl()

        and: "it contains one transmitter"
        def nodeId = "node"
        def transmitter = Mock(Client)
        transmitterPool.add(transmitter)

        and: "change events are monitored"
        def changes = transmitterPool.getChanges()
        def changeListener = Mock(Observer)
        def firstEvent = new TransmitterPoolEvent(transmitter, nodeId, TransmitterPoolEvent.Operation.ASSIGN)
        changes.subscribe(changeListener)

        when: "assigning transmitter with node ID"
        transmitterPool.set(nodeId, transmitter)

        and: "assigning transmitter with the same node ID as previously"
        transmitterPool.set(nodeId, transmitter)

        then: "assignment event was emitted only once"
        1 * changeListener.onNext(firstEvent)
    }

}