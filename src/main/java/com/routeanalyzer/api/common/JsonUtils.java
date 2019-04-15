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
    /**
     * Json Parser
     */

    /**
     * Get Gson with local date time as a date
     * @return Gson object
     */
    public static Gson getGsonLocalDateTime() {
        return new GsonBuilder().setPrettyPrinting().serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeJsonConverter()).create();
    }

    /**
     *
     * @param object
     * @return
     */
    public static String toJson(Object object) {
        return getGsonLocalDateTime().toJson(object);
    }

    public static <T> T fromJson(String jsonStr, Class<T> clazz) {
        return getGsonLocalDateTime().fromJson(jsonStr, clazz);
    }

    /**
     * Json serializer and deserializer for LocalDateTime
     */
    class LocalDateTimeJsonConverter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

        private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;

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
