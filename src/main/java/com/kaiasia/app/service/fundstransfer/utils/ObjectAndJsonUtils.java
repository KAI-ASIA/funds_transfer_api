package com.kaiasia.app.service.fundstransfer.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.kaiasia.app.service.fundstransfer.exception.ExceptionHandler;
import com.kaiasia.app.service.fundstransfer.exception.MapperException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ObjectAndJsonUtils {
    private static final  ObjectMapper mapper = new ObjectMapper();
    private static final  ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

    private ObjectAndJsonUtils() {}

    public static ObjectMapper getObjectMapper() {
        return mapper;
    }

    public static ObjectWriter getObjectWriter() {
        return writer;
    }

    public static <T> T fromJson(String json, Class<T> clazz)  {
        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw ExceptionHandler.transformException(e, MapperException.class);
        }
    }

    public static <T> T fromObject(Object from, Class<T> to)  {
        return mapper.convertValue(from, to);
    }

    public static String toJson(Object obj)  {
        try {
            return writer.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw ExceptionHandler.transformException(e, MapperException.class);
        }
    }
}
