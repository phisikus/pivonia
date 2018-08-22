package eu.phisikus.pivonia.tcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.undercouch.bson4jackson.BsonFactory;
import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.Message;
import io.vavr.control.Try;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class TCPClient implements AutoCloseable, Client {

    private final ObjectMapper mapper = new ObjectMapper(new BsonFactory());
    private SocketChannel clientChannel;


    public TCPClient(String address, int port) throws IOException {
        InetSocketAddress clientAddress = new InetSocketAddress(address, port);
        clientChannel = SocketChannel.open(clientAddress);
        waitForReadyConnection();
    }

    public Try<Integer> send(Message message) {
        return Try.of(() -> sendMessage(message));
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
        boolean waitForConnection = clientChannel.isConnectionPending() && clientChannel.finishConnect();
        if (!waitForConnection) {
            boolean channelIsConnected = clientChannel.isOpen() && clientChannel.isConnected();
            if (!channelIsConnected) {
                throw new IOException("Channel is not open despite making an attempt to connect");
            }
        }
    }

    private ByteBuffer getSerializedMessageAsBuffer(Message message) throws IOException {
        ByteArrayOutputStream serializedMessageStream = new ByteArrayOutputStream();
        mapper.writeValue(serializedMessageStream, message);
        return ByteBuffer.wrap(serializedMessageStream.toByteArray());
    }

    @Override
    public void close() throws Exception {
        clientChannel.close();
    }
}
