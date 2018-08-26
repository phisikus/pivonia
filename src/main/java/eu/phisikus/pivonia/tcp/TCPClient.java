package eu.phisikus.pivonia.tcp;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.Message;
import eu.phisikus.pivonia.api.MessageHandler;
import eu.phisikus.pivonia.converter.BSONConverter;
import io.vavr.control.Try;

import javax.inject.Inject;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
           // TODO implement message reading
        });
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
        clientChannel.close();
    }
}
