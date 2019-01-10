package eu.phisikus.pivonia.tcp.handlers;

import eu.phisikus.pivonia.api.MessageHandler;
import eu.phisikus.pivonia.converter.BSONConverter;
import eu.phisikus.pivonia.utils.BufferUtils;
import io.vavr.collection.List;
import io.vavr.control.Try;
import lombok.extern.log4j.Log4j2;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

@Log4j2
class MessageContentReadHandler implements CompletionHandler<Integer, List<MessageHandler>> {

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
    public void completed(Integer availableBytes, List<MessageHandler> handlers) {
        int bytesMinusSizeHeader = expectedMessageSize - BufferUtils.INT_SIZE;
        if (communicationBuffer.position() >= bytesMinusSizeHeader) {
            deserializeAndHandleMessage(handlers);
            orderNextMessageSizeRead(handlers);
        } else {
            clientChannel.read(communicationBuffer, handlers, this);
        }
    }

    private void orderNextMessageSizeRead(List<MessageHandler> handlers) {
        var messageSizeReadBuffer = BufferUtils.getBufferForMessageSize();
        var readCallback = new MessageSizeReadHandler(bsonConverter, clientChannel, messageSizeReadBuffer);
        clientChannel.read(messageSizeReadBuffer, handlers, readCallback);
    }

    private void deserializeAndHandleMessage(List<MessageHandler> handlers) {
        var messageBuffer = getFullMessageBuffer();
        Try.of(() -> bsonConverter.deserialize(messageBuffer.array()))
                .onSuccess(message -> handleMessage(message, handlers))
                .onFailure(log::error);
    }

    private ByteBuffer getFullMessageBuffer() {
        communicationBuffer.rewind();
        return BufferUtils.getBufferWithCombinedSizeAndContent(expectedMessageSize, communicationBuffer);
    }

    private <T> void handleMessage(T incomingMessage, List<MessageHandler> handlers) {
        var messageType = incomingMessage.getClass();
        handlers.filter(messageHandler -> messageHandler.getMessageType().equals(messageType))
                .forEach(messageHandler -> messageHandler.handleMessage(
                        incomingMessage,
                        new ClientConnectedThroughServer(bsonConverter, clientChannel))
                );
    }

    @Override
    public void failed(Throwable exception, List<MessageHandler> handlers) {
        log.error(exception);
    }
}
