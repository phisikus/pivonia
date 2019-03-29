package eu.phisikus.pivonia.api;

import io.reactivex.Observable;
import io.vavr.control.Try;

public interface Server extends AutoCloseable {

    /**
     * Start listening on given port.
     *
     * @param port number of the port
     * @return instance of the Server  that will handle the traffic
     */
    Try<Server> bind(int port);

    /**
     * Start listening on given port & address.
     *
     * @param address address that the server should listen on
     * @param port    number of the port
     * @return instance of the Server that will handle the traffic
     */
    Try<Server> bind(String address, int port);

    /**
     * Returns messages received by this server that match provided type.
     * The server will not cache messages received prior to this method call.
     * Messages are paired together with client instance that can be used to send response.
     *
     * @param messageType type of messages that will be returned
     * @param <T>         type of message
     * @return observable stream of incoming messages paired with client instance that can be used to send response
     */
    <T> Observable<MessageWithTransmitter<T>> getMessages(Class<T> messageType);

}
