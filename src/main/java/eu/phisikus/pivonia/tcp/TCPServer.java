package eu.phisikus.pivonia.tcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.undercouch.bson4jackson.BsonFactory;
import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.Message;
import eu.phisikus.pivonia.api.MessageHandler;
import io.vavr.control.Try;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;

public class TCPServer implements AutoCloseable {
    private static final String ALL_INTERFACES_ADDRESS = "0.0.0.0";
    private AsynchronousServerSocketChannel serverSocket;
    private AsynchronousChannelGroup serverThreads;

    public TCPServer(int port, MessageHandler messageHandler) throws IOException {
        this(ALL_INTERFACES_ADDRESS, port, messageHandler);
    }


    public TCPServer(String address, int port, MessageHandler messageHandler) throws IOException {
        serverSocket = createAndBindServerSocket(address, port);
        serverSocket.accept(messageHandler, new AcceptHandler(serverSocket));
    }

    private AsynchronousServerSocketChannel createAndBindServerSocket(String address, int port) throws IOException {
        InetSocketAddress serverSocketAddress = new InetSocketAddress(address, port);
        serverThreads = AsynchronousChannelGroup.withFixedThreadPool(
                10,
                Executors.defaultThreadFactory());
        return AsynchronousServerSocketChannel
                .open(serverThreads)
                .bind(serverSocketAddress);
    }


    @Override
    public void close() throws Exception {
        serverSocket.close();
        serverThreads.shutdownNow();
    }

    class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, MessageHandler> {
        static final int COMMUNICATION_BUFFER_SIZE = 1024000;
        private AsynchronousServerSocketChannel serverSocket;

        public AcceptHandler(AsynchronousServerSocketChannel serverSocket) {
            this.serverSocket = serverSocket;
        }

        @Override
        public void completed(AsynchronousSocketChannel clientChannel, MessageHandler messageHandler) {
            serverSocket.accept(null, this);
            var communicationBuffer = ByteBuffer.allocate(COMMUNICATION_BUFFER_SIZE);
            clientChannel.read(communicationBuffer, messageHandler, new ReadHandler(clientChannel, communicationBuffer));
        }

        @Override
        public void failed(Throwable exc, MessageHandler messageHandler) {
            System.out.println(exc);
        }
    }

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


    class ClientConnectedThroughServer implements Client {

        private AsynchronousSocketChannel clientChannel;

        public ClientConnectedThroughServer(AsynchronousSocketChannel clientChannel) {
            this.clientChannel = clientChannel;
        }

        @Override
        public Try<Integer> send(Message message) {
           return Try.failure(null); // TODO implement
        }
    }
}
