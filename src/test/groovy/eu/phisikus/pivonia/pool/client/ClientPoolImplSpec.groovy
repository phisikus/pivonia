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
        def expectedEvent = new ClientChange<>(client, null, ClientChange.Operation.ADD)
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
        def expectedEvent = new ClientChange<>(client, null, ClientChange.Operation.REMOVE)
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

    // TODO add assignment tests
}
