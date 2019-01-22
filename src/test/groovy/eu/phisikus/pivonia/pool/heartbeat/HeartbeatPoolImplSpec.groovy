package eu.phisikus.pivonia.pool.heartbeat

import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.api.MessageWithClient
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.vavr.control.Try
import org.junit.Ignore
import spock.lang.Specification

import java.time.Instant
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class HeartbeatPoolImplSpec extends Specification {

    class HeartbeatLoopbackClient implements Client {

        def messages = PublishSubject.<MessageWithClient>create()

        @Override
        def <T> Try<Client> send(T message) {
            return messages.onNext(new MessageWithClient<>(new HeartbeatMessage("server", Instant.now().toEpochMilli()), this))
        }

        @Override
        Try<Client> connect(String address, int port) {
            return Try.success(this)
        }

        @Override
        <T> Observable<MessageWithClient<T>> getMessages(Class<T> messageType) {
            return messages
        }

        @Override
        void close() throws Exception {

        }
    }


    @Ignore
    def "Should send heartbeat message after adding client to the pool"() {
        given: "there is a client"
        def client = new HeartbeatLoopbackClient()

        and: "empty heartbeat pool"
        final nodeId = UUID.randomUUID()
        def pool = new HeartbeatPoolImpl(500L, 100L, nodeId)

        and: "client is configured to report sent message"
        def messageReceived = new CountDownLatch(1)
        pool.getHeartbeatChanges().subscribe({ event -> messageReceived.countDown() })

        when: "adding client to the heartbeat pool"
        pool.add(client)

        then: "heartbeat message is sent through the client"
        messageReceived.await(10L, TimeUnit.SECONDS) == true

        cleanup: "close heartbeat pool"
        pool.close()
    }
}
