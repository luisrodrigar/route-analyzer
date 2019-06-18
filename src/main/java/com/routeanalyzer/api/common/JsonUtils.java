package com.routeanalyzer.api.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class JsonUtils {

    // Json Parser

    private static Gson gsonLocalDateBuilder = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeJsonConverter())
            .create();

    /**
     * Convert Object to String
     * @param object: any object of any class to transform to String
     * @return json document with the info of the object
     */
    public static String toJson(Object object) {
        return gsonLocalDateBuilder.toJson(object);
    }

    /**
     * Convert String to any class specified in the param clazz
     * @param jsonStr json document
     * @param clazz class of the result of the method
     * @param <T> output class type
     * @return object with the info of the json document
     */
    public static <T> T fromJson(String jsonStr, Class<T> clazz) {
        return gsonLocalDateBuilder.fromJson(jsonStr, clazz);
    }

    /**
     * Json serializer and deserializer for LocalDateTime
     */
    static class LocalDateTimeJsonConverter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

        private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), dateTimeFormatter);
        }

        @Override
        public JsonElement serialize(LocalDateTime localDateTime, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(localDateTime.format(dateTimeFormatter));
        }

    }

}
