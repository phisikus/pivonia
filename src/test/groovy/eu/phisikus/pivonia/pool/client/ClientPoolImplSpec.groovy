package eu.phisikus.pivonia.pool.client

import eu.phisikus.pivonia.api.Client
import io.reactivex.Observer
import spock.lang.Specification

class ClientPoolImplSpec extends Specification {

    def "Should add client to the pool properly"() {
        given: "there is a client"
        def client = Mock(Client)

        and: "an empty pool"
        def clientPool = new ClientPoolImpl()

        and: "change events are monitored"
        def expectedEvent = new ClientEvent<>(client, null, ClientEvent.Operation.ADD)
        def changes = clientPool.getClientChanges()
        def changeListener = Mock(Observer)
        changes.subscribe(changeListener)

        when: "adding the client to the pool"
        clientPool.add(client)

        then: "the pool contains that client"
        clientPool.getClients().contains(client)

        and: "addition event was emitted"
        1 * changeListener.onNext(expectedEvent)
    }

    def "Should remove client from the pool properly"() {
        given: "there is a client"
        def client = Mock(Client)

        and: "client pool with only that one client"
        def clientPool = new ClientPoolImpl()
        clientPool.add(client)

        and: "change events are monitored"
        def expectedEvent = new ClientEvent<>(client, null, ClientEvent.Operation.REMOVE)
        def changes = clientPool.getClientChanges()
        def changeListener = Mock(Observer)
        changes.subscribe(changeListener)

        when: "removing the client to the pool"
        clientPool.remove(client)

        then: "the pool does not contain that client anymore"
        !clientPool.getClients().contains(client)

        and: "deletion event was emitted"
        1 * changeListener.onNext(expectedEvent)
    }

    def "Should assign client to node ID"() {
        given: "there is a client pool"
        def clientPool = new ClientPoolImpl()

        and: "it contains a client"
        def nodeId = "first"
        def client = Mock(Client)
        clientPool.add(client)

        and: "change events are monitored"
        def changes = clientPool.getClientChanges()
        def changeListener = Mock(Observer)
        def expectedEvent = new ClientEvent(client, nodeId, ClientEvent.Operation.ASSIGN)
        changes.subscribe(changeListener)

        when: "assigning client with node ID"
        clientPool.set(nodeId, client)

        then: "that client can be retrieved using node ID"
        clientPool.get(nodeId).get() == client

        and: "assignment event was emitted"
        1 * changeListener.onNext(expectedEvent)

    }

    def "Should unassign client on removal"() {
        given: "there is a client pool"
        def clientPool = new ClientPoolImpl()

        and: "it contains a client"
        def nodeId = "first"
        def client = Mock(Client)
        clientPool.add(client)

        and: "change events are monitored"
        def changes = clientPool.getClientChanges()
        def changeListener = Mock(Observer)
        def assignEvent = new ClientEvent(client, nodeId, ClientEvent.Operation.ASSIGN)
        def unassignEvent = new ClientEvent(client, nodeId, ClientEvent.Operation.UNASSIGN)
        def deleteEvent = new ClientEvent(client, null, ClientEvent.Operation.REMOVE)
        changes.subscribe(changeListener)

        when: "assigning client with node ID"
        clientPool.set(nodeId, client)

        and: "removing it"
        clientPool.remove(client)

        then: "that client cannot be retrieved using node ID"
        !clientPool.get(nodeId).isPresent()

        and: "events were emitted"
        1 * changeListener.onNext(assignEvent)
        1 * changeListener.onNext(unassignEvent)
        1 * changeListener.onNext(deleteEvent)
    }

    def "Should assign new client to node ID"() {
        given: "there is a client pool"
        def clientPool = new ClientPoolImpl()

        and: "it contains two clients"
        def nodeId = "node"
        def client = Mock(Client)
        def secondClient = Mock(Client)
        clientPool.add(client)
        clientPool.add(secondClient)

        and: "change events are monitored"
        def changes = clientPool.getClientChanges()
        def changeListener = Mock(Observer)
        def firstEvent = new ClientEvent(client, nodeId, ClientEvent.Operation.ASSIGN)
        def secondEvent = new ClientEvent(client, nodeId, ClientEvent.Operation.UNASSIGN)
        def thirdEvent = new ClientEvent(secondClient, nodeId, ClientEvent.Operation.ASSIGN)
        changes.subscribe(changeListener)

        when: "assigning client with node ID"
        clientPool.set(nodeId, client)

        and: "assigning second client with the same ID"
        clientPool.set(nodeId, secondClient)

        then: "second client can be retrieved using node ID"
        clientPool.get(nodeId).get() == secondClient

        and: "assignment events were emitted"
        1 * changeListener.onNext(firstEvent)
        1 * changeListener.onNext(secondEvent)
        1 * changeListener.onNext(thirdEvent)
    }


    def "Should update client to node ID assignment"() {
        given: "there is a client pool"
        def clientPool = new ClientPoolImpl()

        and: "it contains one client"
        def nodeId = "node"
        def client = Mock(Client)
        clientPool.add(client)

        and: "change events are monitored"
        def changes = clientPool.getClientChanges()
        def changeListener = Mock(Observer)
        def firstEvent = new ClientEvent(client, nodeId, ClientEvent.Operation.ASSIGN)
        changes.subscribe(changeListener)

        when: "assigning client with node ID"
        clientPool.set(nodeId, client)

        and: "assigning client with the same node ID as previously"
        clientPool.set(nodeId, client)

        then: "assignment event was emitted only once"
        1 * changeListener.onNext(firstEvent)
    }

}