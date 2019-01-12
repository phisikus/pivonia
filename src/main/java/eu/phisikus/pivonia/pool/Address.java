package eu.phisikus.pivonia.pool;

import lombok.Value;

@Value
public class Address {
    private final String hostname;
    private final Integer port;
}
