package eu.phisikus.pivonia.converter.plaintext;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Object that wraps around another objects and stores class name for deserialization
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
class ObjectWrapper {
    @JsonProperty("t")
    String type;

    @JsonProperty("d")
    byte[] data;
}
