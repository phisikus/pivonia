package eu.phisikus.pivonia.tcp.handlers;

import eu.phisikus.pivonia.converter.BSONConverter;
import eu.phisikus.pivonia.utils.BufferUtils;
import io.reactivex.subjects.Subject;
import lombok.extern.log4j.Log4j2;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;


@Log4j2
public class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Map<Class, Subject>> {

    private AsynchronousServerSocketChannel serverSocket;
    private BSONConverter bsonConverter;

    public AcceptHandler(BSONConverter bsonConverter, AsynchronousServerSocketChannel serverSocket) {
        this.bsonConverter = bsonConverter;
        this.serverSocket = serverSocket;
    }

    @Override
    public void completed(AsynchronousSocketChannel clientChannel, Map<Class, Subject> listeners) {
        serverSocket.accept(listeners, this);
        var messageSizeBuffer = BufferUtils.getBufferForMessageSize();
        var readCallback = new MessageSizeReadHandler(bsonConverter, clientChannel, messageSizeBuffer);
        clientChannel.read(messageSizeBuffer, listeners, readCallback);
    }

    @Override
    public void failed(Throwable exception, Map<Class, Subject> listeners) {
        log.error(exception);
    }
}