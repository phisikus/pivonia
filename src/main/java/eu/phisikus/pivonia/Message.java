package eu.phisikus.pivonia;

import lombok.Value;

@Value
public class Message {
    private Long timestamp;
    private String topic;
    private String message;
}
