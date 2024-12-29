package com.kaiasia.app.service.fundstransfer.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ObjectAndJsonUtils {
    private final static ObjectMapper mapper = new ObjectMapper();
    private final static ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

    public static ObjectMapper getObjectMapper() {
        return mapper;
    }

    public static ObjectWriter getObjectWriter() {
        return writer;
    }

    public static <T> T fromJson(String json, Class<T> clazz) throws JsonProcessingException {
        return mapper.readValue(json, clazz);
    }

    public static String toJson(Object obj) throws JsonProcessingException {
        return writer.writeValueAsString(obj);
    }
}
