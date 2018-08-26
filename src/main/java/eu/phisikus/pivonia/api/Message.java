package eu.phisikus.pivonia.api;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class Message {
    private Long timestamp;
    private String topic;
    private String message;
}
