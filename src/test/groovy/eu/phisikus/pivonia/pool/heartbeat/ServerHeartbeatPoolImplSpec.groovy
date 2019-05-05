package eu.phisikus.pivonia.pool.heartbeat

import eu.phisikus.pivonia.api.MessageWithTransmitter
import eu.phisikus.pivonia.api.Server
import eu.phisikus.pivonia.api.Transmitter
import io.reactivex.Observable
import spock.lang.Specification
import spock.lang.Subject

class ServerHeartbeatPoolImplSpec extends Specification {

    def nodeId = UUID.randomUUID()
    @Subject
    def pool = new ServerHeartbeatPoolImpl(50L, 500L, nodeId)

    def "Should add server to the pool and register heartbeat listener properly"() {
        given: "there is server"
        def server = Mock(Server)
        def messages = Observable.empty()

        when: "adding the server to the pool"
        pool.add(server)

        then: "server exists in the pool"
        pool.getServers().contains(server)

        and: "listener is registered"
        1 * server.getMessages(HeartbeatMessage) >> messages
    }

    def "Should remove server from the pool and dispose of heartbeat listener properly"() {
        given: "there is a server"
        def server = Mock(Server)

        and: "heartbeat message stream is defined"
        def isDisposed = false
        def message = GroovyMock(MessageWithTransmitter)

        and: "it contains only one test message"
        def messages = Observable
                .just(message)
                .doOnDispose({ isDisposed = true })
        message.getTransmitter() >> Mock(Transmitter)
        server.getMessages(HeartbeatMessage) >> messages

        and: "server is added to the pool"
        pool.add(server)

        when: "removing server from the pool"
        pool.remove(server)

        then: "server is no longer part of the pool"
        !pool.getServers().contains(server)

        and: "message subscription is disposed"
        isDisposed
    }
    //TODO implement and add tests for events
}
