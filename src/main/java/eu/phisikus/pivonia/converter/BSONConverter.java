package eu.phisikus.pivonia.converter;

import java.io.IOException;

public interface BSONConverter {
    <T> byte[] serialize(T inputObject) throws IOException;

    <T> T deserialize(byte[] serializedObject, Class<T> objectType) throws IOException;
}
