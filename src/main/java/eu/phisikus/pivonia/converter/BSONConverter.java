package eu.phisikus.pivonia.converter;

import java.io.IOException;

/**
 * Serialize and deserialize objects using Binary JSON format
 */
public interface BSONConverter {
    /**
     * Serialize object to bytes.
     *
     * @param inputObject object to be serialized
     * @param <T> type of that object
     * @return array of bytes with serialized object
     * @throws IOException exception if anything goes wrong
     */
    <T> byte[] serialize(T inputObject) throws IOException;

    /**
     * Deserialize object from bytes.
     *
     * @param serializedObject serialized object in form of byte array
     * @param <T> type of object to deserialize
     * @return deserialized object
     * @throws IOException exception thrown if data is broken
     * @throws ClassNotFoundException exception thrown if class to be used is not known
     */
    <T> T deserialize(byte[] serializedObject) throws IOException, ClassNotFoundException;

    /**
     * Enable deserialization for object of certain class.
     * @param type class object of the type
     * @param <T> type of the object
     */
    <T> void enableType(Class<T> type);
}
