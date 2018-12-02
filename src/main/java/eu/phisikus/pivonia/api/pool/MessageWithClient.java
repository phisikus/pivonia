package eu.phisikus.pivonia.api.pool;

import eu.phisikus.pivonia.api.Client;
import lombok.Value;

@Value
public class MessageWithClient<T> {
    private final T message;
    private final Client client;
}
