package eu.phisikus.pivonia.tcp.handlers;

import eu.phisikus.pivonia.api.Transmitter;
import eu.phisikus.pivonia.converter.BSONConverter;
import io.vavr.control.Try;
import lombok.Value;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * This class encapsulates client channel and allows user to send message.
 * Instance will be created each time server receives incoming connection.
 */
@Value
class ClientConnectedToServer implements Transmitter {

    private final BSONConverter bsonConverter;
    private final AsynchronousSocketChannel clientChannel;

    @Override
    public <T> Try<Transmitter> send(T message) {
        try {
            var serializedMessage = ByteBuffer.wrap(bsonConverter.serialize(message));
            writeMessage(serializedMessage);
            return Try.success(this);
        } catch (Throwable throwable) {
            return Try.failure(throwable);
        }
    }

    private void writeMessage(ByteBuffer serializedMessage) {
        while (serializedMessage.hasRemaining()) {
            clientChannel.write(serializedMessage);
        }
    }

    @Override
    public void close() throws Exception {
        clientChannel.close();
    }
}