package eu.phisikus.pivonia.tcp.handlers;

import eu.phisikus.pivonia.api.MessageWithTransmitter;
import eu.phisikus.pivonia.api.Transmitter;
import eu.phisikus.pivonia.converter.BSONConverter;
import eu.phisikus.pivonia.utils.BufferUtils;
import io.reactivex.subjects.Subject;
import io.vavr.control.Try;
import lombok.extern.log4j.Log4j2;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;

@Log4j2
class MessageContentReadHandler implements CompletionHandler<Integer, Map<Class, Subject>> {

    private final AsynchronousSocketChannel clientChannel;
    private final BSONConverter bsonConverter;
    private final ByteBuffer communicationBuffer;
    private final int expectedMessageSize;
    private final ClientConnectedToServer clientConnectedToServer;

    MessageContentReadHandler(ClientConnectedToServer clientConnectedToServer, ByteBuffer communicationBuffer, int expectedMessageSize) {
        this.communicationBuffer = communicationBuffer;
        this.expectedMessageSize = expectedMessageSize;
        this.clientConnectedToServer = clientConnectedToServer;
        this.clientChannel = clientConnectedToServer.getClientChannel();
        this.bsonConverter = clientConnectedToServer.getBsonConverter();
    }

    @Override
    public void completed(Integer availableBytes, Map<Class, Subject> listeners) {
        int bytesMinusSizeHeader = expectedMessageSize - BufferUtils.INT_SIZE;
        if (communicationBuffer.position() >= bytesMinusSizeHeader) {
            deserializeAndHandleMessage(listeners);
            orderNextMessageSizeRead(listeners);
        } else {
            clientChannel.read(communicationBuffer, listeners, this);
        }
    }

    private void orderNextMessageSizeRead(Map<Class, Subject> listeners) {
        var messageSizeReadBuffer = BufferUtils.getBufferForMessageSize();
        var readCallback = new MessageSizeReadHandler(clientConnectedToServer, messageSizeReadBuffer);
        clientChannel.read(messageSizeReadBuffer, listeners, readCallback);
    }

    private void deserializeAndHandleMessage(Map<Class, Subject> listeners) {
        var messageBuffer = getFullMessageBuffer();
        Try.of(() -> bsonConverter.deserialize(messageBuffer.array()))
                .onSuccess(message -> handleMessage(message, listeners))
                .onFailure(log::error);
    }

    private ByteBuffer getFullMessageBuffer() {
        communicationBuffer.rewind();
        return BufferUtils.getBufferWithCombinedSizeAndContent(expectedMessageSize, communicationBuffer);
    }

    private <T> void handleMessage(T incomingMessage, Map<Class, Subject> listeners) {
        var messageType = incomingMessage.getClass();
        var listener = listeners.get(messageType);
        if (listener != null) {
            listener.onNext(
                    new MessageWithTransmitter<>(incomingMessage, clientConnectedToServer)
            );
        }
    }

    @Override
    public void failed(Throwable exception, Map<Class, Subject> listeners) {
        log.error(exception);
    }
}
