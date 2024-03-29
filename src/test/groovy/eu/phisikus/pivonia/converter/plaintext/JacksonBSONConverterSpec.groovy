package eu.phisikus.pivonia.converter.plaintext

import eu.phisikus.pivonia.test.TestMessage
import spock.lang.Specification
import spock.lang.Subject

class JacksonBSONConverterSpec extends Specification {

    @Subject
    def converter = new JacksonBSONConverter()

    def "Object can be serialized to bytes and back"() {

        given: "our test object is a message"
        def expectedObject = new TestMessage(42L, "TestTopic", "TestMessage")

        and: "test object's type is enabled"
        converter.enableType(TestMessage.class)

        when: "serialization is performed and the result is deserialized"
        def serializedObject = converter.serialize(expectedObject)
        def deserializedObject = converter.deserialize(serializedObject)

        then: "deserialized object is equal to the one that was serialized"
        deserializedObject == expectedObject

    }


}
