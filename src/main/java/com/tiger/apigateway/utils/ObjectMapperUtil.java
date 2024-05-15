package com.tiger.apigateway.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ObjectMapperUtil {

    public static ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper()
                // ignore value null
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // setting format date
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // Hack time module to allow 'Z' at the end of string (i.e. javascript json's)
        javaTimeModule.addDeserializer(
                LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME));
        mapper.registerModule(javaTimeModule);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return mapper;
    }

    public static String castToString(Object value) {
        if (value == null) return "";

        try {
            return objectMapper().writeValueAsString(value);
        } catch (Exception e) {
            return "";
        }
    }
}
