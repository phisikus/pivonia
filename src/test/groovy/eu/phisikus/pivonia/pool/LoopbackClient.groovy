package eu.phisikus.pivonia.pool

import eu.phisikus.pivonia.api.Client
import eu.phisikus.pivonia.api.MessageHandler
import io.vavr.control.Try

class LoopbackClient implements Client {

    MessageHandler messageHandler
    boolean isClosed = false

    @Override
    <T> Try<Client> send(T message) {
        messageHandler.handleMessage(message, this)
        return Try.success(this)
    }

    @Override
    Try<Client> connect(String address, int port) {
        return Try.success(this)
    }

    @Override
    <T> Client addHandler(MessageHandler<T> messageHandler) {
        this.messageHandler = messageHandler
        return this
    }

    @Override
    void close() throws Exception {
        isClosed = true
    }
}