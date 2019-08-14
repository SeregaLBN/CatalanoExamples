package ksn.imgusage.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public final class JsonHelper {
    private JsonHelper() {}

    public static <T> T fromJson(String json, Class<T> clazz) throws IOException {
        return new ObjectMapper().readerFor(clazz).readValue(json);
    }

    public static <T> T fromJson(String json, TypeReference<T> clazz) throws IOException {
        return new ObjectMapper().readerFor(clazz).readValue(json);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static ObjectWriter getObjectWriter(boolean formatted) {
        ObjectWriter ow = new ObjectMapper().writer();
        return formatted
                ? ow.withDefaultPrettyPrinter()
                : ow.with(new MinimalPrettyPrinter(""));
    }

    public static void toJson(Object obj, Writer wr) throws IOException {
        toJson(obj, wr, false);
    }
    public static void toJson(Object obj, Writer wr, boolean formatted) throws IOException {
        wr.write(toJson(obj, formatted));
    }

    public static void toJson(Object obj, OutputStream out) throws IOException {
        toJson(obj, out, false);
    }
    public static void toJson(Object obj, OutputStream out, boolean formatted) throws IOException {
        out.write(getObjectWriter(formatted).writeValueAsBytes(obj));
    }

    public static String toJson(Object obj) throws IOException {
        return toJson(obj, false);
    }
    public static String toJson(Object obj, boolean formatted) throws IOException {
            return getObjectWriter(formatted).writeValueAsString(obj);
    }

}
