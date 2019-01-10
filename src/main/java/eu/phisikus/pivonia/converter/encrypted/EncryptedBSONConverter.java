package eu.phisikus.pivonia.converter.encrypted;

import eu.phisikus.pivonia.converter.BSONConverter;
import eu.phisikus.pivonia.crypto.Encryptor;

import java.io.IOException;

/**
 * This BSON Converter provides layer of symmetric authenticated encryption.
 * It uses some other provided converter to do the object-to-bytes serialization.
 * Once the object is serialized the encrypted form is wrapped in a DTO and serialized again.
 * During deserialization the encrypted bytes are extracted from wrapping DTO and decrypted.
 * Once decryption is done, the converter is used again to provide deserialized object.
 */
class EncryptedBSONConverter implements BSONConverter {

    private final BSONConverter bsonConverter;
    private final Encryptor encryptor;

    public EncryptedBSONConverter(BSONConverter bsonConverter, Encryptor encryptor) {
        this.bsonConverter = bsonConverter;
        this.encryptor = encryptor;
    }

    @Override
    public <T> byte[] serialize(T inputObject) throws IOException {
        var serialized = bsonConverter.serialize(inputObject);
        return encryptSerializedData(serialized);
    }

    private byte[] encryptSerializedData(byte[] serialized) throws IOException {
        byte[] encryptedSerialized = encryptor.encrypt(serialized);
        return bsonConverter.serialize(new ByteArrayWrapper(encryptedSerialized));
    }

    @Override
    public <T> T deserialize(byte[] serializedObject) throws IOException, ClassNotFoundException {
        ByteArrayWrapper deserializedEncrypted = bsonConverter.deserialize(serializedObject);
        var decryptedSerialized = encryptor.decrypt(deserializedEncrypted.getData());
        var decryptedDeserialized = bsonConverter.deserialize(decryptedSerialized);
        return (T) decryptedDeserialized;
    }

    @Override
    public <T> void enableType(Class<T> type) {
        bsonConverter.enableType(type);
    }
}
