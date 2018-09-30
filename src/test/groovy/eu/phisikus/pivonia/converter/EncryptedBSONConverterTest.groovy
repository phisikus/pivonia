package eu.phisikus.pivonia.converter

import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.JsonKeysetWriter
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates
import eu.phisikus.pivonia.api.TestMessage
import org.apache.tools.ant.util.FileUtils
import spock.lang.Specification

class EncryptedBSONConverterTest extends Specification {

    private EncryptedBSONConverter converter
    private String testKeyFilename

    void setup() {
        testKeyFilename = buildRandomKeyset()
        def testKeyContent = new FileInputStream(testKeyFilename).getBytes()
        converter = new EncryptedBSONConverter(new JacksonBSONConverter(), testKeyContent)
    }

    void cleanup() {
        FileUtils.delete(new File(testKeyFilename))
    }

    def "Object can be serialized and encrypted to bytes and back"() {

        given: "our test object is a message"
        def expectedObject = new TestMessage(42L, "TestTopic", "TestMessage")

        when: "serialization and encryption is performed and the decrypted result is deserialized"
        def serializedObject = converter.serialize(expectedObject)
        def deserializedObject = converter.deserialize(serializedObject, TestMessage.class)

        then: "deserialized object is equal to the one that was serialized"
        deserializedObject == expectedObject

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
