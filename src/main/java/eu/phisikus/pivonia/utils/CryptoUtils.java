package eu.phisikus.pivonia.utils;

import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetWriter;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AesGcmKeyManager;
import com.google.crypto.tink.subtle.Random;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

public class CryptoUtils {

    private static final int RANDOM_BYTES_SIZE = 100;

    public static byte[] getKeysetContent(String keysetFileName) throws IOException {
        return Files.readAllBytes(Paths.get(keysetFileName));
    }

    public static byte[] getRandomBytes() {
        return Random.randBytes(RANDOM_BYTES_SIZE);
    }

    public static void buildKeyset(String testKeyFilename) throws GeneralSecurityException, IOException {
        AeadConfig.register();
        var keysetHandle = KeysetHandle.generateNew(AesGcmKeyManager.aes128GcmTemplate());
        var keysetWriter = JsonKeysetWriter.withPath(testKeyFilename);
        CleartextKeysetHandle.write(keysetHandle, keysetWriter);
    }
}
