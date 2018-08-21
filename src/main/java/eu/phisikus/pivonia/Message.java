package eu.phisikus.pivonia;

import java.util.Objects;
import java.util.StringJoiner;

public class Message {
    private Long timestamp;
    private String topic;
    private String message;

    public Message() {
    }

    public Message(Long timestamp, String topic, String message) {
        this.timestamp = timestamp;
        this.topic = topic;
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getTopic() {
        return topic;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message1 = (Message) o;
        return Objects.equals(timestamp, message1.timestamp) &&
                Objects.equals(topic, message1.topic) &&
                Objects.equals(message, message1.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, topic, message);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Message.class.getSimpleName() + "[", "]")
                .add("timestamp=" + timestamp)
                .add("topic='" + topic + "'")
                .add("message='" + message + "'")
                .toString();
    }
}
