package eu.phisikus.pivonia.pool.heartbeat

import eu.phisikus.pivonia.pool.heartbeat.events.HeartbeatPoolEvent
import eu.phisikus.pivonia.pool.heartbeat.test.HeartbeatLoopbackClient
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ClientHeartbeatPoolImplSpec extends Specification {

    def "Should perform heartbeat protocol iteration after adding client to the pool"() {
        given: "there is a client"
        def client = new HeartbeatLoopbackClient(true)

        and: "empty heartbeat pool"
        final nodeId = UUID.randomUUID()
        def pool = new ClientHeartbeatPoolImpl(50L, 500L, nodeId)

        and: "heartbeat pool events are monitored"
        def messageReceived = new CountDownLatch(3)
        pool.getHeartbeatChanges()
                .filter({ event -> event.getOperation() == HeartbeatPoolEvent.Operation.RECEIVED })
                .subscribe({ event -> messageReceived.countDown() })

        when: "adding client to the heartbeat pool"
        pool.add(client)

        then: "heartbeat process is successful and event is emitted"
        messageReceived.await(5L, TimeUnit.SECONDS)

        cleanup: "close heartbeat pool"
        pool.dispose()
    }

    def "Should perform heartbeat protocol and register timeout"() {
        given: "there is a client connected to dead server"
        def client = new HeartbeatLoopbackClient(false)

        and: "empty heartbeat pool"
        final nodeId = UUID.randomUUID()
        def pool = new ClientHeartbeatPoolImpl(50L, 500L, nodeId)

        and: "heartbeat pool events are monitored"
        def messageReceived = new CountDownLatch(1)
        pool.getHeartbeatChanges()
                .filter({ event -> event.getOperation() == HeartbeatPoolEvent.Operation.TIMEOUT })
                .subscribe({ event -> messageReceived.countDown() })

        when: "adding client to the heartbeat pool"
        pool.add(client)

        then: "heartbeat process responds with timeout event"
        messageReceived.await(500L, TimeUnit.SECONDS)

        cleanup: "close heartbeat pool"
        pool.dispose()
    }

    def "Should remove client from heartbeat pool"() {
        given: "there is a client"
        def client = new HeartbeatLoopbackClient(false)

        and: "empty heartbeat pool"
        final nodeId = UUID.randomUUID()
        def pool = new ClientHeartbeatPoolImpl(1000L, 2000L, nodeId)

        when: "adding client to the heartbeat pool"
        pool.add(client)

        then: "removing it from the pool is successful"
        pool.remove(client)

        cleanup: "close heartbeat pool"
        pool.dispose()
    }
}
