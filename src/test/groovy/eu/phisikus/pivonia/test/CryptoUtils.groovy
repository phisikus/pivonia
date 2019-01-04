package eu.phisikus.pivonia.test

import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.JsonKeysetWriter
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates

import java.nio.charset.Charset

class CryptoUtils {
    public static getRandomBytes() {
        UUID.randomUUID().toString().getBytes(Charset.defaultCharset())
    }

    public static buildRandomKeyset() {
        AeadConfig.register()
        def keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES128_GCM)
        def testKeyFilename = UUID.randomUUID().toString() + ".json"
        def keysetWriter = JsonKeysetWriter.withPath(testKeyFilename)
        CleartextKeysetHandle.write(keysetHandle, keysetWriter)
        testKeyFilename
    }

    public static getKeysetContent(String keysetFileName) {
        new FileInputStream(keysetFileName).getBytes()
    }
}
