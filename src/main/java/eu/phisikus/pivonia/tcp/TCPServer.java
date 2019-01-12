package eu.phisikus.pivonia.tcp;

import eu.phisikus.pivonia.api.MessageWithClient;
import eu.phisikus.pivonia.api.Server;
import eu.phisikus.pivonia.converter.BSONConverter;
import eu.phisikus.pivonia.tcp.handlers.AcceptHandler;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import io.vavr.control.Try;

import javax.inject.Inject;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class TCPServer implements Server {
    private static final String ALL_INTERFACES_ADDRESS = "0.0.0.0";
    private AsynchronousServerSocketChannel serverSocket;
    private AsynchronousChannelGroup serverThreads;
    private BSONConverter bsonConverter;
    private Map<Class, Subject> listeners = new ConcurrentHashMap<>();

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
    public Try<Server> bind(int port) {
        return bind(ALL_INTERFACES_ADDRESS, port);
    }

    @Override
    public Try<Server> bind(String address, int port) {
        TCPServer newServer = new TCPServer(bsonConverter);
        newServer.listeners = new ConcurrentHashMap<>(listeners);
        try {
            newServer.serverSocket = newServer.createAndBindServerSocket(address, port);
        } catch (IOException e) {
            return Try.failure(e);
        }
        newServer.serverSocket.accept(newServer.listeners, new AcceptHandler(bsonConverter, newServer.serverSocket));
        return Try.success(newServer);
    }

    @Override
    public <T> Observable<MessageWithClient<T>> getMessages(Class<T> messageType) {
        bsonConverter.enableType(messageType);
        listeners.putIfAbsent(messageType, PublishSubject.create());
        return listeners.get(messageType);
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
