package eu.phisikus.pivonia.tcp.handlers;

import eu.phisikus.pivonia.api.Message;
import eu.phisikus.pivonia.api.MessageHandler;
import eu.phisikus.pivonia.converter.BSONConverter;
import io.vavr.control.Try;
import lombok.extern.log4j.Log4j2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import static eu.phisikus.pivonia.tcp.handlers.AcceptHandler.INT_SIZE;

@Log4j2
class MessageContentReadHandler implements CompletionHandler<Integer, MessageHandler> {

    private final AsynchronousSocketChannel clientChannel;
    private final ByteBuffer communicationBuffer;
    private final BSONConverter bsonConverter;
    private final int expectedMessageSize;

    MessageContentReadHandler(BSONConverter bsonConverter, AsynchronousSocketChannel clientChannel, ByteBuffer communicationBuffer, int expectedMessageSize) {
        this.bsonConverter = bsonConverter;
        this.clientChannel = clientChannel;
        this.communicationBuffer = communicationBuffer;
        this.expectedMessageSize = expectedMessageSize;
    }

    @Override
    public void completed(Integer availableBytes, MessageHandler messageHandler) {
        int bytesMinusSizeHeader = expectedMessageSize - INT_SIZE;
        if (communicationBuffer.position() >= bytesMinusSizeHeader) {
            deserializeAndHandleMessage(messageHandler);
            orderNextMessageSizeRead(messageHandler);
        } else {
            clientChannel.read(communicationBuffer, messageHandler, this);
        }
    }

    private void orderNextMessageSizeRead(MessageHandler messageHandler) {
        var messageSizeReadBuffer = ByteBuffer.allocate(INT_SIZE);
        var readCallback = new MessageSizeReadHandler(bsonConverter, clientChannel, messageSizeReadBuffer);
        clientChannel.read(messageSizeReadBuffer, messageHandler, readCallback);
    }

    private void deserializeAndHandleMessage(MessageHandler messageHandler) {
        ByteBuffer messageBuffer = getFullMessageBuffer();
        Try.of(() -> bsonConverter.deserialize(messageBuffer.array(), Message.class))
                .onSuccess(message -> handleMessage(message, messageHandler))
                .onFailure(log::error);
    }

    private ByteBuffer getFullMessageBuffer() {
        communicationBuffer.rewind();
        var messageSizeBuffer = ByteBuffer
                .allocate(INT_SIZE)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(expectedMessageSize)
                .rewind();

        return ByteBuffer.allocate(expectedMessageSize*2)
                .put(messageSizeBuffer)
                .put(communicationBuffer)
                .rewind();
    }

    private void handleMessage(Message incomingMessage, MessageHandler messageHandler) {
        messageHandler.handleMessage(incomingMessage, new ClientConnectedThroughServer(clientChannel));
    }

    @Override
    public void failed(Throwable exception, MessageHandler messageQueue) {
        log.error(exception);
    }
}
