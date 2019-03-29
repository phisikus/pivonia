package eu.phisikus.pivonia.tcp.handlers;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.MessageWithTransmitter;
import eu.phisikus.pivonia.converter.BSONConverter;
import io.reactivex.Observable;
import io.vavr.control.Try;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

class ClientConnectedThroughServer implements Client {

    private AsynchronousSocketChannel clientChannel;
    private BSONConverter bsonConverter;

    ClientConnectedThroughServer(BSONConverter bsonConverter, AsynchronousSocketChannel clientChannel) {
        this.bsonConverter = bsonConverter;
        this.clientChannel = clientChannel;
    }

    @Override
    public <T> Try<Client> send(T message) {
        try {
            var serializedMessage = ByteBuffer.wrap(bsonConverter.serialize(message));
            writeMessage(serializedMessage);
            return Try.success(this);
        } catch (IOException exception) {
            return Try.failure(exception);
        }
    }

    @Override
    public Try<Client> connect(String address, int port) {
        return Try.success(this);
    }

    @Override
    public <T> Observable<MessageWithTransmitter<T>> getMessages(Class<T> messageType) {
        throw new IllegalStateException("This client does not return any messages, associated server does");
    }

    private void writeMessage(ByteBuffer serializedMessage) {
        while (serializedMessage.hasRemaining()) {
            clientChannel.write(serializedMessage);
        }
    }


    @Override
    public void close() throws Exception {
        // no need to close anything
    }
}