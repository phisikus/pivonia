package eu.phisikus.pivonia.tcp;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.MessageHandler;
import eu.phisikus.pivonia.converter.BSONConverter;
import eu.phisikus.pivonia.utils.BufferUtils;
import io.vavr.collection.List;
import io.vavr.control.Try;
import lombok.extern.log4j.Log4j2;

import javax.inject.Inject;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Log4j2
public class TCPClient implements Client {

    private SocketChannel clientChannel;
    private BSONConverter bsonConverter;
    private ExecutorService messageListener = Executors.newSingleThreadExecutor();
    private List<MessageHandler> handlers = List.empty();

    @Inject
    public TCPClient(BSONConverter bsonConverter) {
        this.bsonConverter = bsonConverter;
    }


    @Override
    public Try<Client> connect(String address, int port) {
        try {
            TCPClient newClient = getNewOpenClient(address, port);
            newClient.waitForReadyConnection();
            newClient.bindMessageHandlers(handlers);
            return Try.success(newClient);
        } catch (IOException e) {
            return Try.failure(e);
        }
    }

    @Override
    public <T> Client addHandler(MessageHandler<T> messageHandler) {
        handlers = handlers.push(messageHandler);
        return this;
    }

    private void bindMessageHandlers(List<MessageHandler> handlers) {
        messageListener.submit(() -> {
            try {
                listenForMessages(handlers);
            } catch (IOException | ClassNotFoundException exception) {
                log.error(exception);
            }
        });
    }

    private void listenForMessages(List<MessageHandler> handlers) throws IOException, ClassNotFoundException {
        registerTypes(handlers);
        while (clientChannel.isOpen()) {
            int messageSize = readMessageSize();
            if (messageSize > 0) {
                readAndHandleMessage(messageSize, handlers);
            } else {
                break;
            }
        }
    }

    private void registerTypes(List<MessageHandler> handlers) {
        handlers.forEach(messageHandler -> bsonConverter.enableType(messageHandler.getMessageType()));
    }

    private void readAndHandleMessage(int messageSize, List<MessageHandler> handlers) throws IOException, ClassNotFoundException {
        var contentBuffer = readMessageContent(messageSize);
        var messageBuffer = BufferUtils.getBufferWithCombinedSizeAndContent(messageSize, contentBuffer);
        var incomingMessage = bsonConverter.deserialize(messageBuffer.array());
        handleMessage(incomingMessage, handlers);
    }

    private <T> void handleMessage(T incomingMessage, List<MessageHandler> handlers) {
        var messageType = incomingMessage.getClass();
        handlers.filter(messageHandler -> messageHandler.getMessageType().equals(messageType))
                .forEach(messageHandler -> messageHandler.handleMessage(incomingMessage, this));
    }

    private ByteBuffer readMessageContent(int messageSize) throws IOException {
        int contentSize = messageSize - BufferUtils.INT_SIZE;
        ByteBuffer contentBuffer = ByteBuffer.allocate(contentSize);
        int bytesRead = 0;
        while (bytesRead < contentSize) {
            bytesRead += clientChannel.read(contentBuffer);
        }
        return contentBuffer.rewind();
    }

    private int readMessageSize() throws IOException {
        var messageSizeBuffer = ByteBuffer.allocate(BufferUtils.INT_SIZE);
        int bytesRead = 0;
        while (bytesRead < BufferUtils.INT_SIZE) {
            bytesRead += clientChannel.read(messageSizeBuffer);
        }
        return BufferUtils.readMessageSizeFromBuffer(messageSizeBuffer);
    }

    private TCPClient getNewOpenClient(String address, int port) throws IOException {
        var newClient = new TCPClient(bsonConverter);
        var clientAddress = new InetSocketAddress(address, port);
        newClient.clientChannel = SocketChannel.open(clientAddress);
        newClient.handlers = handlers;
        return newClient;
    }


    public <T> Try<Client> send(T message) {
        try {
            sendMessage(message);
        } catch (Exception e) {
            return Try.failure(e);
        }
        return Try.success(this);
    }


    private <T> Integer sendMessage(T message) throws IOException {
        waitForReadyConnection();
        var serializedMessageBuffered = getSerializedMessageAsBuffer(message);
        int bytesSent = 0;
        while (serializedMessageBuffered.hasRemaining()) {
            bytesSent += clientChannel.write(serializedMessageBuffered);
        }
        return bytesSent;
    }

    private void waitForReadyConnection() throws IOException {
        if (clientChannel == null) {
            throw new RuntimeException("You forgot to connect the client first!");
        }

        boolean waitForConnection = clientChannel.isConnectionPending() && clientChannel.finishConnect();
        if (!waitForConnection) {
            boolean channelIsConnected = clientChannel.isOpen() && clientChannel.isConnected();
            if (!channelIsConnected) {
                throw new IOException("Channel is not open despite making an attempt to connect");
            }
        }
    }

    private <T> ByteBuffer getSerializedMessageAsBuffer(T message) throws IOException {
        return ByteBuffer.wrap(bsonConverter.serialize(message));
    }

    @Override
    public void close() throws Exception {
        sendClose();
        clientChannel.close();
        messageListener.shutdownNow();
    }

    private void sendClose() throws IOException {
        var zeroMessageSize = BufferUtils.getBufferWithMessageSize(0);
        clientChannel.write(zeroMessageSize);
    }
}
