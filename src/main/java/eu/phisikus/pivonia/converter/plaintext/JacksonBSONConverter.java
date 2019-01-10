package eu.phisikus.pivonia.converter.plaintext;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.undercouch.bson4jackson.BsonFactory;
import eu.phisikus.pivonia.converter.BSONConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

class JacksonBSONConverter implements BSONConverter {
    private final ObjectMapper mapper;
    private final Set<Class> supportedTypes;

    public JacksonBSONConverter() {
        mapper = new ObjectMapper(new BsonFactory());
        supportedTypes = new HashSet<>();
    }

    public <T> byte[] serialize(T inputObject) throws IOException {
        byte[] objectContent = serializeObject(inputObject);
        var wrapper = new ObjectWrapper(inputObject.getClass().getName(), objectContent);
        return serializeObject(wrapper);
    }

    private <T> byte[] serializeObject(T inputObject) throws IOException {
        try (var output = new ByteArrayOutputStream()) {
            mapper.writeValue(output, inputObject);
            return output.toByteArray();
        }
    }

    public <T> T deserialize(byte[] serializedObject) throws IOException, ClassNotFoundException {
        var wrapper = mapper.readValue(serializedObject, ObjectWrapper.class);
        return extractObjectFromWrapper(wrapper);
    }

    private <T> T extractObjectFromWrapper(ObjectWrapper wrapper) throws ClassNotFoundException, IOException {
        var type = (Class<T>) Class.forName(wrapper.getType());
        if (supportedTypes.contains(type)) {
            return mapper.readValue(wrapper.getData(), type);
        }
        throw new ClassNotFoundException("Provided type was not among legal classes to deserialize");
    }


    @Override
    public <T> void enableType(Class<T> type) {
        supportedTypes.add(type);
    }

}
