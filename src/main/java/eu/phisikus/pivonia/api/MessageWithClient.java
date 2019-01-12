package eu.phisikus.pivonia.api;

import lombok.Value;

@Value
public class MessageWithClient<T> {
    private final T message;
    private final Client client;
}
