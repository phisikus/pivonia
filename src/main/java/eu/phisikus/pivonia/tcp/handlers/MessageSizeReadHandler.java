package eu.phisikus.pivonia.tcp.handlers;

import eu.phisikus.pivonia.converter.BSONConverter;
import eu.phisikus.pivonia.utils.BufferUtils;
import io.reactivex.subjects.Subject;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;

@Log4j2
class MessageSizeReadHandler implements CompletionHandler<Integer, Map<Class, Subject>> {

    private final AsynchronousSocketChannel clientChannel;
    private final ByteBuffer communicationBuffer;
    private final BSONConverter bsonConverter;

    MessageSizeReadHandler(BSONConverter bsonConverter, AsynchronousSocketChannel clientChannel, ByteBuffer communicationBuffer) {
        this.bsonConverter = bsonConverter;
        this.clientChannel = clientChannel;
        this.communicationBuffer = communicationBuffer;
    }

    @Override
    public void completed(Integer bytesReceived, Map<Class, Subject> listeners) {
        boolean messageSizeReceived = bytesReceived.equals(BufferUtils.INT_SIZE);
        if (messageSizeReceived) {
            readMessageContent(listeners);
        } else {
            if (bytesReceived > 0) {
                retryRead(listeners);
            }
        }
    }

    private void readMessageContent(Map<Class, Subject> listeners) {
        int messageSize = BufferUtils.readMessageSizeFromBuffer(communicationBuffer);
        if (messageSize > 0) {
            var messageBuffer = ByteBuffer.allocate(messageSize);
            var contentReadHandler = new MessageContentReadHandler(bsonConverter, clientChannel, messageBuffer, messageSize);
            clientChannel.read(messageBuffer, listeners, contentReadHandler);
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

    private void retryRead(Map<Class, Subject> listeners) {
        clientChannel.read(communicationBuffer, listeners, this);
    }


    @Override
    public void failed(Throwable exception, Map<Class, Subject> listeners) {
        log.error(exception);
    }
}
