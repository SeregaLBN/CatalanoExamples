package ksn.imgusage.utils;

import javax.ws.rs.core.GenericType;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.JsonbException;

public final class JsonHelper {
    private JsonHelper() {}

    public static String toJson(Object obj, boolean formatted) {
        JsonbConfig cfg = new JsonbConfig();
        cfg.withFormatting(formatted);

        try (Jsonb jsonb = JsonbBuilder.create(cfg)) {
            return jsonb.toJson(obj);
        } catch (Exception ex) {
            throw new JsonbException("JSON write failed", ex);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try (Jsonb jsonb = JsonbBuilder.create()) {
            return jsonb.fromJson(json, clazz);
        } catch (Exception ex) {
            throw new JsonbException("JSON read failed", ex);
        }
    }

    public static <T> T fromJson(String json, GenericType<T> clazz) {
        try (Jsonb jsonb = JsonbBuilder.create()) {
            return jsonb.fromJson(json, clazz.getType());
        } catch (Exception ex) {
            throw new JsonbException("JSON read failed", ex);
        }
    }

}
