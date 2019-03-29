package eu.phisikus.pivonia.api;

import io.reactivex.Observable;

public interface Receiver {
    /**
     * Returns messages received by this receiver that match provided type.
     * The receiver will not cache messages received prior to this method call.
     *
     * @param messageType type of messages that will be returned
     * @param <T>         type of message
     * @return observable stream of incoming messages paired with Transmitter instance that can be used to send response.
     */
    <T> Observable<MessageWithTransmitter<T>> getMessages(Class<T> messageType);
}
