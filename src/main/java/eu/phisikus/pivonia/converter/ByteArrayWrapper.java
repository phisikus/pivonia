package eu.phisikus.pivonia.converter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This object is used to wrap around simple array of bytes for easier serialization
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
class ByteArrayWrapper {
    private byte[] data;
}
