package eu.phisikus.pivonia.tcp;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.Message;
import eu.phisikus.pivonia.api.MessageHandler;
import eu.phisikus.pivonia.converter.BSONConverter;
import eu.phisikus.pivonia.utils.BufferUtils;
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

    @Inject
    public TCPClient(BSONConverter bsonConverter) {
        this.bsonConverter = bsonConverter;
    }


    @Override
    public Try<Client> connect(String address, int port, MessageHandler messageHandler) {
        try {
            TCPClient newClient = getNewOpenClient(address, port);
            newClient.waitForReadyConnection();
            newClient.bindMessageHandler(messageHandler);
            return Try.success(newClient);
        } catch (IOException e) {
            return Try.failure(e);
        }
    }

    private void bindMessageHandler(MessageHandler messageHandler) {
        messageListener.submit(() -> {
            try {
                listenForMessages(messageHandler);
            } catch (IOException exception) {
                log.error(exception);
            }
        });
    }

    private void listenForMessages(MessageHandler messageHandler) throws IOException {
        while (clientChannel.isOpen()) {
            int messageSize = readMessageSize();
            if (messageSize > 0) {
                readAndHandleMessage(messageSize, messageHandler);
            } else {
                break;
            }
        }
    }

    private void readAndHandleMessage(int messageSize, MessageHandler messageHandler) throws IOException {
        var contentBuffer = readMessageContent(messageSize);
        var messageBuffer = BufferUtils.getBufferWithCombinedSizeAndContent(messageSize, contentBuffer);
        var incomingMessage = bsonConverter.deserialize(messageBuffer.array(), Message.class);
        messageHandler.handleMessage(incomingMessage, this);
    }

    private ByteBuffer readMessageContent(int messageSize) throws IOException {
        ByteBuffer contentBuffer = ByteBuffer.allocate(messageSize - BufferUtils.INT_SIZE);
        clientChannel.read(contentBuffer);
        return contentBuffer;
    }

    private int readMessageSize() throws IOException {
        var messageSizeBuffer = ByteBuffer.allocate(4);
        int bytesRead = 0;
        while (bytesRead < 4) {
            bytesRead += clientChannel.read(messageSizeBuffer);
        }
        return BufferUtils.readMessageSizeFromBufer(messageSizeBuffer);
    }

    private TCPClient getNewOpenClient(String address, int port) throws IOException {
        var newClient = new TCPClient(bsonConverter);
        var clientAddress = new InetSocketAddress(address, port);
        newClient.clientChannel = SocketChannel.open(clientAddress);
        return newClient;
    }


    public Try<Client> send(Message message) {
        try {
            sendMessage(message);
        } catch (IOException e) {
            return Try.failure(e);
        }
        return Try.success(this);
    }


    private Integer sendMessage(Message message) throws IOException {
        waitForReadyConnection();
        ByteBuffer serializedMessageBuffered = getSerializedMessageAsBuffer(message);
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

    private ByteBuffer getSerializedMessageAsBuffer(Message message) throws IOException {
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
