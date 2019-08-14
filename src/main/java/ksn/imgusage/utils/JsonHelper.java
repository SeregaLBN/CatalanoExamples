package ksn.imgusage.utils;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public final class JsonHelper {
    private JsonHelper() {}

    public static <T> T fromJson(InputStream inputStream, TypeReference<T> clazz) throws IOException {
        return new ObjectMapper().readerFor(clazz).readValue(inputStream);
    }

    public static <T> T fromJson(JsonNode node, Class<T> clazz) throws IOException {
        return new ObjectMapper().readerFor(clazz).readValue(node);
    }

    private static ObjectWriter getObjectWriter(boolean formatted) {
        ObjectWriter ow = new ObjectMapper().writer();
        return formatted
                ? ow.withDefaultPrettyPrinter()
                : ow.with(new MinimalPrettyPrinter(""));
    }

    public static String toJson(Object obj, boolean formatted) throws IOException {
        return getObjectWriter(formatted).writeValueAsString(obj);
    }

}
