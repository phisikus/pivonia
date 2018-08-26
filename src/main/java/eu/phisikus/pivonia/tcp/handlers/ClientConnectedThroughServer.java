package eu.phisikus.pivonia.tcp.handlers;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.Message;
import eu.phisikus.pivonia.api.MessageHandler;
import io.vavr.control.Try;

import java.nio.channels.AsynchronousSocketChannel;

class ClientConnectedThroughServer implements Client {

    private AsynchronousSocketChannel clientChannel;

    ClientConnectedThroughServer(AsynchronousSocketChannel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public Try<Client> send(Message message) {
        return Try.failure(null); // TODO implement message sending.
    }

    @Override
    public Try<Client> connect(String address, int port, MessageHandler messageHandler) {
        return Try.success(this); // we are already connected
    }

    @Override
    public void close() throws Exception {
        // no need to close anything
    }
}