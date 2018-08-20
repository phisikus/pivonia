import com.fasterxml.jackson.databind.ObjectMapper
import de.undercouch.bson4jackson.BsonFactory
import spock.lang.Specification

class BsonSerializationTest extends Specification {


    def "object can be serialized to BSON"() {
        given:
        def expectedObject = new TestSerializable("Text", 42)

        and:
        def output = new ByteArrayOutputStream()
        def mapper = new ObjectMapper(new BsonFactory())


        when:
        mapper.writeValue(output, expectedObject)
        def actualOutput = output.toByteArray()
        def actualOutputAsInput = new ByteArrayInputStream(actualOutput)
        def actualObject = mapper.readValue(actualOutputAsInput, TestSerializable.class)

        then:
        actualObject == expectedObject
    }
}
