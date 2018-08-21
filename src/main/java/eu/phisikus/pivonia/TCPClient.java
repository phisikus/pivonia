package eu.phisikus.pivonia;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.undercouch.bson4jackson.BsonFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class TCPClient implements AutoCloseable{

    private final InetSocketAddress clientAddress;
    private final SocketChannel clientChannel;
    private final ObjectMapper mapper = new ObjectMapper(new BsonFactory());

    public TCPClient(String address, int port) throws IOException {
        clientAddress = new InetSocketAddress(address, port);
        clientChannel = SocketChannel.open(clientAddress);
        boolean waitForConnection = clientChannel.isConnectionPending() && clientChannel.finishConnect();
        if (!waitForConnection) {
            boolean channelIsConnected = clientChannel.isOpen() && clientChannel.isConnected();
            if (!channelIsConnected) {
                throw new IOException("Channel is not open despite making an attempt to connect");
            }
        }
    }

    public void send(Message message) throws IOException {
        ByteBuffer serializedMessageBuffered = getSerializedMessageAsBuffer(message);
        while (serializedMessageBuffered.hasRemaining()) {
            clientChannel.write(serializedMessageBuffered);
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
