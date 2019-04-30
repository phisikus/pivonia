package eu.phisikus.pivonia.converter.encrypted;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

/**
 * This object is used to wrap around simple array of bytes for easier serialization
 */
@Value
class ByteArrayWrapper {
    @JsonProperty("d")
    private byte[] data;
}
