package eu.phisikus.pivonia.api;

import lombok.Value;

@Value
public class MessageWithTransmitter<T> {
    private final T message;
    private final Transmitter transmitter;
}
