package eu.phisikus.pivonia.converter;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * This BSON Converter provides
 */
class EncryptedBSONConverter implements BSONConverter {

    private final BSONConverter bsonConverter;
    private final Aead encoder;

    public EncryptedBSONConverter(BSONConverter bsonConverter, byte[] keyContent) throws GeneralSecurityException, IOException {
        AeadConfig.register();
        var keysetReader = JsonKeysetReader.withBytes(keyContent);
        var keysetHandle = CleartextKeysetHandle.read(keysetReader);
        this.encoder = AeadFactory.getPrimitive(keysetHandle);
        this.bsonConverter = bsonConverter;
    }

    @Override
    public <T> byte[] serialize(T inputObject) throws IOException {
        var serialized = bsonConverter.serialize(inputObject);
        return encryptSerializedData(serialized);
    }

    private byte[] encryptSerializedData(byte[] serialized) throws IOException {
        try {
            byte[] encryptedSerialized = encoder.encrypt(serialized, null);
            return bsonConverter.serialize(new ByteArrayWrapper(encryptedSerialized));
        } catch (GeneralSecurityException exception) {
            throw new IOException("Security problem appeared during message encryption", exception);
        }
    }

    @Override
    public <T> T deserialize(byte[] serializedObject, Class<T> objectType) throws IOException {
        var deserializedEncrypted = bsonConverter.deserialize(serializedObject, ByteArrayWrapper.class);
        try {
            var decryptedSerialized = encoder.decrypt(deserializedEncrypted.getData(), null);
            var decryptedDeserialized = bsonConverter.deserialize(decryptedSerialized, objectType);
            return decryptedDeserialized;
        } catch (GeneralSecurityException exception) {
            throw new IOException("Security problem appeared during message decryption", exception);
        }
    }
}
