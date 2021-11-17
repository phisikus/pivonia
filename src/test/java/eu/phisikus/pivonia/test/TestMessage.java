package eu.phisikus.pivonia.test;

import lombok.Value;

@Value
public class TestMessage {
    private Long timestamp;
    private String topic;
    private String message;
}
