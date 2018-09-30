package eu.phisikus.pivonia.crypto;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;

class SymmetricalEncryptor implements Encryptor {

    private final Aead encoder;

    SymmetricalEncryptor(byte[] keyContent) {
        try {
            AeadConfig.register();
            var keysetReader = JsonKeysetReader.withBytes(keyContent);
            var keysetHandle = CleartextKeysetHandle.read(keysetReader);
            this.encoder = AeadFactory.getPrimitive(keysetHandle);
        } catch (GeneralSecurityException | IOException exception) {
            throw new RuntimeException(exception);
        }
    }


    @Override
    public byte[] encrypt(byte[] cleartextData) {
        try {
            return encoder.encrypt(cleartextData, null);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] decrypt(byte[] encryptedData) {
        try {
            return encoder.decrypt(encryptedData, null);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
