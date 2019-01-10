package eu.phisikus.pivonia.tcp.handlers;

import eu.phisikus.pivonia.api.MessageHandler;
import eu.phisikus.pivonia.converter.BSONConverter;
import eu.phisikus.pivonia.utils.BufferUtils;
import io.vavr.collection.List;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

@Log4j2
class MessageSizeReadHandler implements CompletionHandler<Integer, List<MessageHandler>> {

    private final AsynchronousSocketChannel clientChannel;
    private final ByteBuffer communicationBuffer;
    private final BSONConverter bsonConverter;

    MessageSizeReadHandler(BSONConverter bsonConverter, AsynchronousSocketChannel clientChannel, ByteBuffer communicationBuffer) {
        this.bsonConverter = bsonConverter;
        this.clientChannel = clientChannel;
        this.communicationBuffer = communicationBuffer;
    }

    @Override
    public void completed(Integer bytesReceived, List<MessageHandler> handlers) {
        boolean messageSizeReceived = bytesReceived.equals(BufferUtils.INT_SIZE);
        if (messageSizeReceived) {
            readMessageContent(handlers);
        } else {
            if (bytesReceived > 0) {
                retryRead(handlers);
            }
        }
    }

    private void readMessageContent(List<MessageHandler> handlers) {
        int messageSize = BufferUtils.readMessageSizeFromBuffer(communicationBuffer);
        if (messageSize > 0) {
            var messageBuffer = ByteBuffer.allocate(messageSize);
            var contentReadHandler = new MessageContentReadHandler(bsonConverter, clientChannel, messageBuffer, messageSize);
            clientChannel.read(messageBuffer, handlers, contentReadHandler);
        } else {
            closeCommunication();
        }

    }

    private void closeCommunication() {
        try {
            clientChannel.close();
        } catch (IOException exception) {
            log.error(exception);
        }
    }

    private void retryRead(List<MessageHandler> handlers) {
        clientChannel.read(communicationBuffer, handlers, this);
    }


    @Override
    public void failed(Throwable exception, List<MessageHandler> handlers) {
        log.error(exception);
    }
}
