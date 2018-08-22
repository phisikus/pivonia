package eu.phisikus.pivonia.tcp.handlers;

import eu.phisikus.pivonia.api.MessageHandler;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, MessageHandler> {
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