package ua.alexlapada;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JacksonUtil {
    private static final ObjectMapper jsonMapper = init(null);
    private static final ObjectMapper yamlMapper = init(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));

    private static ObjectMapper init(JsonFactory factory) {
        ObjectMapper objectMapper = factory == null ? new ObjectMapper() : new ObjectMapper(factory);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

    public static <T> T convert(Object content, Class<T> clazz) {
        return jsonMapper.convertValue(content, clazz);
    }

    public static <T> T convert(Object content, TypeReference<T> typeReference) {
        return jsonMapper.convertValue(content, typeReference);
    }

    public static <T> T read(String content, TypeReference<T> typeReference) throws IOException {
        return jsonMapper.readValue(content, typeReference);
    }

    public static <T> T readJson(InputStream content, Class<T> clazz) throws IOException {
        return jsonMapper.readValue(content, clazz);
    }

    public static <T> T readJson(byte[] content, Class<T> clazz) throws IOException {
        return jsonMapper.readValue(content, clazz);
    }

    public static void writeJson(OutputStream outputStream, Object content) throws IOException {
        jsonMapper.writeValue(outputStream, content);
    }

    public static <T> T readJson(InputStream content, TypeReference<T> typeReference) throws IOException {
        return jsonMapper.readValue(content, typeReference);
    }

    public static <T> T readJson(String content, TypeReference<T> typeReference) throws IOException {
        return jsonMapper.readValue(content, typeReference);
    }

    public static <T> MappingIterator<T> readJsonLineSeparated(byte[] content, TypeReference<T> typeReference) throws IOException {
        return jsonMapper.readerFor(typeReference).readValues(content);
    }

    public static <T> T readJson(String content, Class<T> clazz) throws IOException {
        return jsonMapper.readValue(content, clazz);
    }

    public static String writeJson(Object content) throws IOException {
        return jsonMapper.writeValueAsString(content);
    }

    public static <T> T readYaml(InputStream inputStream, Class<T> clazz) throws IOException {
        return yamlMapper.readValue(inputStream, clazz);
    }

    public static <T> T readYaml(String content, TypeReference<T> typeReference) throws IOException {
        return yamlMapper.readValue(content, typeReference);
    }

    public static <T> T readYaml(String content, Class<T> clazz) throws IOException {
        return yamlMapper.readValue(content, clazz);
    }

    public static <T> T readYaml(JsonNode content, Class<T> clazz) throws IOException {
        return yamlMapper.treeToValue(content, clazz);
    }
}
