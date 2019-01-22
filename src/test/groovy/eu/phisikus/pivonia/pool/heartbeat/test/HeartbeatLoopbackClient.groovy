package eu.phisikus.pivonia.pool.heartbeat.test

import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.api.MessageWithClient
import eu.phisikus.pivonia.pool.heartbeat.HeartbeatMessage
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.vavr.control.Try

import java.time.Instant

class HeartbeatLoopbackClient implements Client {

    def messages = PublishSubject.<MessageWithClient> create()
    def isAlive = false

    HeartbeatLoopbackClient(isAlive) {
        this.isAlive = isAlive
    }

    @Override
    <T> Try<Client> send(T message) {
        if (isAlive) {
            def heartbeatResponse = new HeartbeatMessage("node1", Instant.now().toEpochMilli())
            messages.onNext(new MessageWithClient<>(heartbeatResponse, this))
            return Try.success(this)
        }
        return Try.failure(new RuntimeException("Could not send message."))
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