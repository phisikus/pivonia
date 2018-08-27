package eu.phisikus.pivonia.tcp.handlers;

import eu.phisikus.pivonia.api.MessageHandler;
import eu.phisikus.pivonia.converter.BSONConverter;
import eu.phisikus.pivonia.utils.BufferUtils;
import lombok.extern.log4j.Log4j2;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;


@Log4j2
public class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, MessageHandler> {

    private AsynchronousServerSocketChannel serverSocket;
    private BSONConverter bsonConverter;

    public AcceptHandler(BSONConverter bsonConverter, AsynchronousServerSocketChannel serverSocket) {
        this.bsonConverter = bsonConverter;
        this.serverSocket = serverSocket;
    }

    @Override
    public void completed(AsynchronousSocketChannel clientChannel, MessageHandler messageHandler) {
        serverSocket.accept(null, this);
        var messageSizeBuffer = BufferUtils.getBufferForMessageSize();
        var readCallback = new MessageSizeReadHandler(bsonConverter, clientChannel, messageSizeBuffer);
        clientChannel.read(messageSizeBuffer, messageHandler, readCallback);
    }

    @Override
    public void failed(Throwable exception, MessageHandler messageHandler) {
        log.error(exception);
    }
}