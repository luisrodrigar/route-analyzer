package com.routeanalyzer.api.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vavr.control.Try;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class JsonUtils {

    // Json Parser
    private static ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);


    /**
     * Convert Object to String
     * @param object: any object of any class to transform to String
     * @return json document with the info of the object
     */
    public static Try<String> toJson(Object object) {
        return Try.of(() -> objectMapper.writeValueAsString(object))
                .onFailure(err -> log.error("Error trying to convert from class to json string", err));
    }

    /**
     * Convert String to any class specified in the param clazz
     * @param jsonStr json document
     * @param clazz result type class
     * @param <T> output class type
     * @return object with the info of the json document
     */
    public static <T> Try<T> fromJson(String jsonStr, Class<T> clazz) {
        return Try.of(() -> objectMapper.readValue(jsonStr, clazz))
                .onFailure(err -> log.error("Error trying to convert from json string {} to class {} ", jsonStr,
                        clazz, err));
    }

}
