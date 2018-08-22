package eu.phisikus.pivonia.tcp.handlers;

import eu.phisikus.pivonia.api.Client;
import eu.phisikus.pivonia.api.Message;
import io.vavr.control.Try;

import java.nio.channels.AsynchronousSocketChannel;

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