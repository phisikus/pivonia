package eu.phisikus.pivonia.tcp;

import eu.phisikus.pivonia.api.MessageHandler;
import eu.phisikus.pivonia.api.Server;
import eu.phisikus.pivonia.converter.BSONConverter;
import eu.phisikus.pivonia.tcp.handlers.AcceptHandler;
import io.vavr.control.Try;

import javax.inject.Inject;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.Executors;

public class TCPServer implements Server {
    private static final String ALL_INTERFACES_ADDRESS = "0.0.0.0";
    private AsynchronousServerSocketChannel serverSocket;
    private AsynchronousChannelGroup serverThreads;
    private BSONConverter bsonConverter;

    @Inject
    public TCPServer(BSONConverter bsonConverter) {
        this.bsonConverter = bsonConverter;
    }

    @Override
    public void close() throws Exception {
        serverSocket.close();
        serverThreads.shutdownNow();
    }

    @Override
    public Try<Server> bind(int port, MessageHandler messageHandler) {
        return bind(ALL_INTERFACES_ADDRESS, port, messageHandler);
    }

    @Override
    public Try<Server> bind(String address, int port, MessageHandler messageHandler) {
        TCPServer newServer = new TCPServer(bsonConverter);
        try {
            newServer.serverSocket = newServer.createAndBindServerSocket(address, port);
        } catch (IOException e) {
            return Try.failure(e);
        }
        newServer.serverSocket.accept(messageHandler, new AcceptHandler(bsonConverter, newServer.serverSocket));
        return Try.success(newServer);
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
}
