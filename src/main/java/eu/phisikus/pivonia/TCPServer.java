package eu.phisikus.pivonia;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.undercouch.bson4jackson.BsonFactory;
import io.vavr.control.Try;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

public class TCPServer implements AutoCloseable {
    private static final String ALL_INTERFACES_ADDRESS = "0.0.0.0";
    private AsynchronousServerSocketChannel serverSocket;
    private AsynchronousChannelGroup serverThreads;
    private Queue<Message> messageQueue = new ConcurrentLinkedQueue<>();

    public TCPServer(int port) throws IOException {
        this(ALL_INTERFACES_ADDRESS, port);
    }


    public TCPServer(String address, int port) throws IOException {
        serverSocket = createAndBindServerSocket(address, port);
        serverSocket.accept(messageQueue, new AcceptHandler(serverSocket));
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

    class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Queue<Message>> {
        static final int COMMUNICATION_BUFFER_SIZE = 1024000;
        private AsynchronousServerSocketChannel serverSocket;

        public AcceptHandler(AsynchronousServerSocketChannel serverSocket) {
            this.serverSocket = serverSocket;
        }

        @Override
        public void completed(AsynchronousSocketChannel clientChannel, Queue<Message> messageQueue) {
            serverSocket.accept(null, this);
            var communicationBuffer = ByteBuffer.allocate(COMMUNICATION_BUFFER_SIZE);
            clientChannel.read(communicationBuffer, messageQueue, new ReadHandler(clientChannel, communicationBuffer));
        }

        @Override
        public void failed(Throwable exc, Queue<Message> attachment) {
            System.out.println(exc);
        }
    }

    class ReadHandler implements CompletionHandler<Integer, Queue<Message>> {

        private final AsynchronousSocketChannel clientChannel;
        private final ByteBuffer communicationBuffer;
        private final ObjectMapper mapper = new ObjectMapper(new BsonFactory());

        public ReadHandler(AsynchronousSocketChannel clientChannel, ByteBuffer communicationBuffer) {
            this.clientChannel = clientChannel;
            this.communicationBuffer = communicationBuffer;
        }

        @Override
        public void completed(Integer bytesReceived, Queue<Message> messageQueue) {
            if (bytesReceived == -1) {
                System.out.println("Client disconnected. Current queue:");
                messageQueue.forEach(System.out::println);
            }
            if (bytesReceived > 0) {
                communicationBuffer.rewind();
                Try.of(() -> mapper.readValue(communicationBuffer.array(), Message.class))
                        .map(messageQueue::add);
                clientChannel.read(communicationBuffer, messageQueue, this);
            }
        }

        @Override
        public void failed(Throwable exc, Queue<Message> messageQueue) {
            System.out.println(exc);
        }
    }
}
