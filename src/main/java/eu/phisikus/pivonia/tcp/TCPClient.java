package eu.phisikus.pivonia.tcp;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.MessageWithClient;
import eu.phisikus.pivonia.converter.BSONConverter;
import eu.phisikus.pivonia.utils.BufferUtils;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import io.vavr.control.Try;
import lombok.extern.log4j.Log4j2;

import javax.inject.Inject;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Log4j2
public class TCPClient implements Client {

    private SocketChannel clientChannel;
    private BSONConverter bsonConverter;
    private ExecutorService messageListener = Executors.newSingleThreadExecutor();
    private Map<Class, Subject> listeners = new ConcurrentHashMap<>();


    @Inject
    public TCPClient(BSONConverter bsonConverter) {
        this.bsonConverter = bsonConverter;
    }


    @Override
    public Try<Client> connect(String address, int port) {
        try {
            var newClient = getNewOpenClient(address, port);
            newClient.waitForReadyConnection();
            newClient.bindMessageListeners(newClient.listeners);
            return Try.success(newClient);
        } catch (IOException e) {
            return Try.failure(e);
        }
    }

    @Override
    public <T> Observable<MessageWithClient<T>> getMessages(Class<T> messageType) {
        bsonConverter.enableType(messageType);
        listeners.putIfAbsent(messageType, PublishSubject.create());
        return listeners.get(messageType);
    }


    private void bindMessageListeners(Map<Class, Subject> listeners) {
        messageListener.submit(() -> {
            try {
                listenForMessages(listeners);
            } catch (IOException | ClassNotFoundException exception) {
                log.error(exception);
            }
        });
    }

    private void listenForMessages(Map<Class, Subject> listeners) throws IOException, ClassNotFoundException {
        while (clientChannel.isOpen()) {
            int messageSize = readMessageSize();
            if (messageSize > 0) {
                readAndHandleMessage(messageSize, listeners);
            } else {
                break;
            }
        }
    }


    private void readAndHandleMessage(int messageSize, Map<Class, Subject> listeners)
            throws IOException, ClassNotFoundException {
        var contentBuffer = readMessageContent(messageSize);
        var messageBuffer = BufferUtils.getBufferWithCombinedSizeAndContent(messageSize, contentBuffer);
        var incomingMessage = bsonConverter.deserialize(messageBuffer.array());
        handleMessage(incomingMessage, listeners);
    }

    private <T> void handleMessage(T incomingMessage, Map<Class, Subject> listeners) {
        var messageType = incomingMessage.getClass();
        var listener = listeners.get(messageType);
        if (listener != null) {
            listener.onNext(new MessageWithClient<>(incomingMessage, this));
        }
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
        newClient.listeners = new ConcurrentHashMap<>(listeners);
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
        listeners.clear();
    }

    private void sendClose() throws IOException {
        var zeroMessageSize = BufferUtils.getBufferWithMessageSize(0);
        clientChannel.write(zeroMessageSize);
    }
}
