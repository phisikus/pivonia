package eu.phisikus.pivonia.converter.encrypted

import eu.phisikus.pivonia.api.TestMessage
import eu.phisikus.pivonia.converter.BSONConverter
import eu.phisikus.pivonia.crypto.Encryptor
import spock.lang.Specification
import spock.lang.Subject

class EncryptedBSONConverterSpec extends Specification {

    def mockConverter = Mock(BSONConverter)
    def mockEncryptor = Mock(Encryptor)
    def mockData = getFakeBytes()
    def mockEncryptedData = getFakeBytes()
    def mockWrappedEncryptedData = new ByteArrayWrapper(mockEncryptedData)
    def mockEncryptedSerializedData = getFakeBytes()

    @Subject
    def converter = new EncryptedBSONConverter(mockConverter, mockEncryptor)


    def "Object can be serialized and encrypted to bytes and back"() {

        given: "our test object is a message"
        def expectedObject = new TestMessage(42L, "TestTopic", "TestMessage")

        when: "serialization and encryption is performed and the decrypted result is deserialized"
        def serializedObject = converter.serialize(expectedObject)
        def deserializedObject = converter.deserialize(serializedObject, TestMessage.class)

        then: "deserialized object is equal to the one that was serialized"
        deserializedObject == expectedObject

        and: "mock encryptor and converter were used properly"
        1 * mockConverter.serialize(expectedObject) >> mockData
        1 * mockEncryptor.encrypt(mockData) >> mockEncryptedData
        1 * mockConverter.serialize(mockWrappedEncryptedData) >> mockEncryptedSerializedData
        1 * mockConverter.deserialize(mockEncryptedSerializedData, ByteArrayWrapper) >> mockWrappedEncryptedData
        1 * mockEncryptor.decrypt(mockEncryptedData) >> mockData
        1 * mockConverter.deserialize(mockData, TestMessage) >> expectedObject

    }

    private byte[] getFakeBytes() {
        return UUID.randomUUID().toString().getBytes()
    }

}
