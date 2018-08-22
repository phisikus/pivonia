package eu.phisikus.pivonia;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class Message {
    private Long timestamp;
    private String topic;
    private String message;
}
