package eu.phisikus.pivonia.tcp.handlers;

import eu.phisikus.pivonia.api.Transmitter;
import eu.phisikus.pivonia.converter.BSONConverter;
import io.vavr.control.Try;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

class ClientConnectedToServer implements Transmitter {

    private AsynchronousSocketChannel clientChannel;
    private BSONConverter bsonConverter;

    ClientConnectedToServer(BSONConverter bsonConverter, AsynchronousSocketChannel clientChannel) {
        this.bsonConverter = bsonConverter;
        this.clientChannel = clientChannel;
    }

    @Override
    public <T> Try<Transmitter> send(T message) {
        try {
            var serializedMessage = ByteBuffer.wrap(bsonConverter.serialize(message));
            writeMessage(serializedMessage);
            return Try.success(this);
        } catch (IOException exception) {
            return Try.failure(exception);
        }
    }

    private void writeMessage(ByteBuffer serializedMessage) {
        while (serializedMessage.hasRemaining()) {
            clientChannel.write(serializedMessage);
        }
    }
}