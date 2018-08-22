package eu.phisikus.pivonia.tcp;

import eu.phisikus.pivonia.api.MessageHandler;
import eu.phisikus.pivonia.tcp.handlers.AcceptHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
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

}
