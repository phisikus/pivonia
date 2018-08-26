package eu.phisikus.pivonia.tcp.handlers;

import eu.phisikus.pivonia.api.MessageHandler;
import eu.phisikus.pivonia.converter.BSONConverter;
import lombok.extern.log4j.Log4j2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

@Log4j2
class MessageSizeReadHandler implements CompletionHandler<Integer, MessageHandler> {

    private final AsynchronousSocketChannel clientChannel;
    private final ByteBuffer communicationBuffer;
    private final BSONConverter bsonConverter;

    MessageSizeReadHandler(BSONConverter bsonConverter, AsynchronousSocketChannel clientChannel, ByteBuffer communicationBuffer) {
        this.bsonConverter = bsonConverter;
        this.clientChannel = clientChannel;
        this.communicationBuffer = communicationBuffer;
    }

    @Override
    public void completed(Integer bytesReceived, MessageHandler messageHandler) {
        boolean messageSizeReceived = bytesReceived.equals(AcceptHandler.INT_SIZE);
        if (messageSizeReceived) {
            readMessageContent(messageHandler);
        } else {
            if (bytesReceived > 0) {
                retryRead(messageHandler);
            }
        }
    }

    private void readMessageContent(MessageHandler messageHandler) {
        int messageSize = communicationBuffer
                .rewind()
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();
        var messageBuffer = ByteBuffer.allocate(messageSize);
        var contentReadHandler = new MessageContentReadHandler(bsonConverter, clientChannel, messageBuffer, messageSize);
        clientChannel.read(messageBuffer, messageHandler, contentReadHandler);
    }

    private void retryRead(MessageHandler messageHandler) {
        clientChannel.read(communicationBuffer, messageHandler, this);
    }


    @Override
    public void failed(Throwable exception, MessageHandler messageQueue) {
        log.error(exception);
    }
}
