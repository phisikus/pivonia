package eu.phisikus.pivonia.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.undercouch.bson4jackson.BsonFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class JacksonBSONConverter implements BSONConverter {
    private ObjectMapper mapper = new ObjectMapper(new BsonFactory());

    public <T> byte[] serialize(T inputObject) throws IOException {
        var output = new ByteArrayOutputStream();
        mapper.writeValue(output, inputObject);
        return output.toByteArray();
    }

    public <T> T deserialize(byte[] serializedObject, Class<T> objectType) throws IOException {
        return mapper.readValue(serializedObject, objectType);
    }


}
