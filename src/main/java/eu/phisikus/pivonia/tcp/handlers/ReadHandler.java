package eu.phisikus.pivonia.tcp.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.undercouch.bson4jackson.BsonFactory;
import eu.phisikus.pivonia.api.Message;
import eu.phisikus.pivonia.api.MessageHandler;
import io.vavr.control.Try;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

class ReadHandler implements CompletionHandler<Integer, MessageHandler> {

    private final AsynchronousSocketChannel clientChannel;
    private final ByteBuffer communicationBuffer;
    private final ObjectMapper mapper = new ObjectMapper(new BsonFactory());

    public ReadHandler(AsynchronousSocketChannel clientChannel, ByteBuffer communicationBuffer) {
        this.clientChannel = clientChannel;
        this.communicationBuffer = communicationBuffer;
    }

    @Override
    public void completed(Integer bytesReceived, MessageHandler messageHandler) {
        if (bytesReceived > 0) {
            communicationBuffer.rewind();
            Try.of(() -> mapper.readValue(communicationBuffer.array(), Message.class))
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
