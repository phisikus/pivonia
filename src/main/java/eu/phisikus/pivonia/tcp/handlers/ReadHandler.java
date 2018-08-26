package eu.phisikus.pivonia.tcp.handlers;

import eu.phisikus.pivonia.api.Message;
import eu.phisikus.pivonia.api.MessageHandler;
import eu.phisikus.pivonia.converter.BSONConverter;
import io.vavr.control.Try;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

class ReadHandler implements CompletionHandler<Integer, MessageHandler> {

    private final AsynchronousSocketChannel clientChannel;
    private final ByteBuffer communicationBuffer;
    private final BSONConverter bsonConverter;

    ReadHandler(BSONConverter bsonConverter, AsynchronousSocketChannel clientChannel, ByteBuffer communicationBuffer) {
        this.bsonConverter = bsonConverter;
        this.clientChannel = clientChannel;
        this.communicationBuffer = communicationBuffer;
    }

    @Override
    public void completed(Integer bytesReceived, MessageHandler messageHandler) {
        if (bytesReceived > 0) {
            communicationBuffer.rewind();
            Try.of(() -> bsonConverter.deserialize(communicationBuffer.array(), Message.class))
                    .forEach(message -> handleMessage(message, messageHandler));

            clientChannel.read(communicationBuffer, messageHandler, this);
        }
    }

    private void handleMessage(Message incomingMessage, MessageHandler messageHandler) {
        messageHandler.handleMessage(incomingMessage, new ClientConnectedThroughServer(clientChannel));
    }

    @Override
    public void failed(Throwable exc, MessageHandler messageQueue) {
        System.out.println(exc);
    }
}
