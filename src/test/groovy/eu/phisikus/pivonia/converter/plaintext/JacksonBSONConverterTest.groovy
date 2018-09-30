package eu.phisikus.pivonia.converter.plaintext


import eu.phisikus.pivonia.api.TestMessage
import spock.lang.Shared
import spock.lang.Specification

class JacksonBSONConverterTest extends Specification {

    @Shared
    def converter = new JacksonBSONConverter()

    def "Object can be serialized to bytes and back"() {

        given: "our test object is a message"
        def expectedObject = new TestMessage(42L, "TestTopic", "TestMessage")

        when: "serialization is performed and the result is deserialized"
        def serializedObject = converter.serialize(expectedObject)
        def deserializedObject = converter.deserialize(serializedObject, TestMessage.class)

        then: "deserialized object is equal to the one that was serialized"
        deserializedObject == expectedObject

    }


}
