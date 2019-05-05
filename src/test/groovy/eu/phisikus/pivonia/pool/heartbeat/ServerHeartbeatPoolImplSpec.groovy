package eu.phisikus.pivonia.pool.heartbeat

import eu.phisikus.pivonia.api.MessageWithTransmitter
import eu.phisikus.pivonia.api.Server
import eu.phisikus.pivonia.api.Transmitter
import eu.phisikus.pivonia.pool.heartbeat.events.ReceivedEvent
import eu.phisikus.pivonia.pool.heartbeat.events.TimeoutEvent
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import spock.lang.Specification
import spock.lang.Subject
import spock.util.concurrent.PollingConditions

import java.time.Instant

class ServerHeartbeatPoolImplSpec extends Specification {

    def nodeId = UUID.randomUUID()

    @Subject
    def pool = new ServerHeartbeatPoolImpl(500L, 500L, nodeId)

    def polling = new PollingConditions(timeout: 10)

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

        cleanup: "server is removed from the pool"
        pool.remove(server)
    }

    def "Should emmit event when heartbeat message is received"() {
        given: "there is a server"
        def nodeId = "node0"
        def server = Mock(Server)

        and: "heartbeat message stream is defined with single message"
        def sender = Mock(Transmitter)
        def messageContent = new HeartbeatMessage(nodeId, 0L)
        def message = new MessageWithTransmitter(messageContent, sender)
        def messages = Observable.just(message)
        server.getMessages(HeartbeatMessage) >> messages

        and: "heartbeat pool reception events are monitored"
        ReceivedEvent actualEvent
        pool.getHeartbeatChanges()
                .filter({ it instanceof ReceivedEvent })
                .subscribe({ actualEvent = it })

        when: "server is added to the pool"
        pool.add(server)

        then: "heartbeat reception event is emitted"
        verifyAll(actualEvent) {
            id == nodeId
            transmitter == sender
        }

        cleanup: "server is removed from the pool"
        pool.remove(server)
    }

    def "Should remove server from the pool and dispose of heartbeat listener properly"() {
        given: "there is a server"
        def server = Mock(Server)

        and: "heartbeat message stream is defined"
        def transmitter = Mock(Transmitter)
        def messageContent = new HeartbeatMessage("node2", Instant.now().toEpochMilli())
        def message = new MessageWithTransmitter(messageContent, transmitter)

        and: "it contains only one test message"
        Disposable subscription
        def messages = Observable
                .just(message)
                .doOnSubscribe({ subscription = it })

        server.getMessages(HeartbeatMessage) >> messages

        and: "server is added to the pool"
        pool.add(server)

        when: "removing server from the pool"
        pool.remove(server)

        then: "server is no longer part of the pool"
        !pool.getServers().contains(server)

        and: "message subscription is disposed"
        subscription.isDisposed()
    }

    def "Should emmit event when timeout occurs"() {
        given: "there is a server"
        def nodeId = "node1"
        def server = Mock(Server)

        and: "heartbeat message stream is defined with single message"
        def sender = Mock(Transmitter)
        def currentTime = Instant.now().toEpochMilli()
        def messageContent = new HeartbeatMessage(nodeId, currentTime)
        def message = new MessageWithTransmitter(messageContent, sender)
        def messages = Observable.just(message)
        server.getMessages(HeartbeatMessage) >> messages

        and: "heartbeat pool timeout events are monitored"
        def timeoutEvents = []
        pool.getHeartbeatChanges()
                .filter({ it instanceof TimeoutEvent })
                .subscribe({ timeoutEvents.push(it) })

        when: "server is added to the pool"
        pool.add(server)

        then: "heartbeat timeout event is emitted"
        polling.eventually {
            verifyAll(timeoutEvents) {
                timeoutEvents.size() == 1
                verifyAll(timeoutEvents.first() as TimeoutEvent) {
                    transmitter == sender
                }
            }
        }

        cleanup: "server is removed from the pool"
        pool.remove(server)
    }

}
