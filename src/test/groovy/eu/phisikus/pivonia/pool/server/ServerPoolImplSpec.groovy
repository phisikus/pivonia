package eu.phisikus.pivonia.pool.server

import eu.phisikus.pivonia.api.Server
import spock.lang.Specification
import spock.lang.Subject

class ServerPoolImplSpec extends Specification {

    @Subject
    def serverPool = new ServerPoolImpl()

    def "Should add server to the pool properly"() {
        given: "server is defined"
        def server = Mock(Server)

        and: "event changes are monitored"
        def actualEvent = _
        def eventSubscription = serverPool.getServerChanges().subscribe({ actualEvent = it })

        when: "addition is performed"
        serverPool.add(server)

        then: "server belongs to the pool"
        serverPool.getServers().contains(server)

        and: "addition event was emitted"
        actualEvent == new ServerEvent(server, ServerEvent.Operation.ADD)

        cleanup:
        eventSubscription.dispose()
    }

    def "Should remove server from the pool properly"() {
        given: "server is defined"
        def server = Mock(Server)

        and: "it belongs to the pool"
        serverPool.add(server)

        and: "event changes are monitored"
        def actualEvent = _
        def eventSubscription = serverPool.getServerChanges().subscribe({ actualEvent = it })

        when: "removal operation is performed"
        serverPool.remove(server)

        then: "server no longer belongs to the pool"
        !serverPool.getServers().contains(server)

        and: "deletion event was emitted"
        actualEvent == new ServerEvent(server, ServerEvent.Operation.REMOVE)

        cleanup:
        eventSubscription.dispose()
    }
}
