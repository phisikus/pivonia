package eu.phisikus.pivonia.pool.heartbeat

import eu.phisikus.pivonia.api.Server
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import spock.lang.Specification
import spock.lang.Subject

class ServerHeartbeatPoolImplSpec extends Specification {

    def nodeId = UUID.randomUUID()
    @Subject
    def pool = new ServerHeartbeatPoolImpl(nodeId)

    def "Should add server to the pool and register heartbeat listener properly"() {
        given: "there is server"
        def server = Mock(Server)

        when: "adding the server to the pool"
        pool.add(server)

        then: "server exists in the pool"
        pool.getServers().contains(server)

        and: "listener is registered"
        1 * server.getMessages(HeartbeatMessage) >> Mock(Observable)
    }

    def "Should remove server from the pool and dispose of heartbeat listener properly"() {
        given: "there is server in the heartbeat pool"
        def server = Mock(Server)
        def messages = Mock(Observable)
        def subscription = Mock(Disposable)
        server.getMessages(HeartbeatMessage) >> messages
        messages.subscribe(_) >> subscription
        pool.add(server)

        when: "removing server from the pool"
        pool.remove(server)

        then: "server is no longer part of the pool"
        !pool.getServers().contains(server)

        and: "listener is disposed"
        1 * subscription.dispose()
    }
}
