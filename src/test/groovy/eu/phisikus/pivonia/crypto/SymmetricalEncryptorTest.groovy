package eu.phisikus.pivonia.crypto

import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.JsonKeysetWriter
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates
import org.apache.tools.ant.util.FileUtils
import spock.lang.Shared
import spock.lang.Specification

import java.nio.charset.Charset

class SymmetricalEncryptorTest extends Specification {

    @Shared
    private Encryptor encryptor
    private String testKeyFilename

    void setup() {
        testKeyFilename = buildRandomKeyset()
        def testKeyContent = new FileInputStream(testKeyFilename).getBytes()
        encryptor = new SymmetricalEncryptor(testKeyContent)
    }

    void cleanup() {
        FileUtils.delete(new File(testKeyFilename))
    }


    def "Object can be and encrypted and decrypted"() {

        given: "there is a set of random input bytes"
        def expectedBytes = getRandomBytes()

        when: "encryption and decryption is performed"
        def encryptedBytes = encryptor.encrypt(expectedBytes)
        def actualBytes = encryptor.decrypt(encryptedBytes)

        then: "decrypted object is equal to the one that was serialized"
        actualBytes == expectedBytes

    }

    private byte[] getRandomBytes() {
        UUID.randomUUID().toString().getBytes(Charset.defaultCharset())
    }

    private String buildRandomKeyset() {
        AeadConfig.register()
        def keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES128_GCM)
        def testKeyFilename = UUID.randomUUID().toString() + ".json"
        def keysetWriter = JsonKeysetWriter.withPath(testKeyFilename)
        CleartextKeysetHandle.write(keysetHandle, keysetWriter)
        testKeyFilename
    }
}
