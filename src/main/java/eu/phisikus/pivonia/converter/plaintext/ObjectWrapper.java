package eu.phisikus.pivonia.converter.plaintext;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

/**
 * Object that wraps around another objects and stores class name for deserialization
 */
@Value
class ObjectWrapper {
    @JsonProperty("t")
    String type;

    @JsonProperty("d")
    byte[] data;
}
