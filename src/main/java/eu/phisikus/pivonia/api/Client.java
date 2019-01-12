package eu.phisikus.pivonia.api;

import io.reactivex.Observable;
import io.vavr.control.Try;

public interface Client extends AutoCloseable {
    /**
     * Send message to connected client.
     *
     * @param message message that will be sent to the connected client
     * @return if successful return itself, otherwise exception that occurred
     */
    <T> Try<Client> send(T message);


    /**
     * Connect the client using provided address.
     *
     * @param address address of the client
     * @param port    port of the client
     * @return client connected to given address or exception that occurred
     */
    Try<Client> connect(String address, int port);


    /**
     * Returns messages received by this client that match provided type.
     * The client will not cache messages received prior to this method call.
     *
     * @param messageType type of messages that will be returned
     * @param <T>         type of message
     * @return observable stream of incoming messages paired with client instance that can be used to send response.
     */
    <T> Observable<MessageWithClient<T>> getMessages(Class<T> messageType);
}
