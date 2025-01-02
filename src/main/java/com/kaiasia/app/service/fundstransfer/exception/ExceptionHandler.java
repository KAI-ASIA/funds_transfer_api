package com.kaiasia.app.service.fundstransfer.exception;

import com.kaiasia.app.core.model.ApiError;
import com.kaiasia.app.core.model.ApiHeader;
import com.kaiasia.app.core.model.ApiRequest;
import com.kaiasia.app.core.model.ApiResponse;
import com.kaiasia.app.core.utils.GetErrorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;

@Component
@Slf4j
public class ExceptionHandler {
    @Autowired
    private GetErrorUtils getErrorUtils;

    public ApiResponse handle(Function<ApiRequest, ApiResponse> process, ApiRequest apiRequest, String location) {
        ApiResponse response;
        try {
            response = process.apply(apiRequest);
        } catch (Exception e) {
            response = new ApiResponse();
            ApiHeader header = apiRequest.getHeader();
            header.setReqType("RESPONSE");
            response.setHeader(header);
            response.setError(exceptionResolver(e, location));
        }
        return response;
    }

    private ApiError exceptionResolver(Exception e, String location) {
        log.error("{}:{}", location, e.getMessage());
        ApiError error;
        if (e instanceof InsertFailedException) {
            error = getErrorUtils.getError("501", new String[]{e.getMessage()});
            return error;
        }
        if (e instanceof UpdateFailedException) {
            error = getErrorUtils.getError("502", new String[]{e.getMessage()});
            return error;
        }
        if (e instanceof RestClientException) {
            error = getErrorUtils.getError("505", new String[]{e.getMessage()});
            return error;
        }
        if (e instanceof MapperException) {
            error = getErrorUtils.getError("600", new String[]{e.getMessage()});
            return error;
        }
        error = getErrorUtils.getError("999", new String[]{e.getMessage()});
        return error;
    }

    public static <T extends Throwable> T transformException(Exception from, Class<T> to) {
        try {
            return to.getConstructor(Throwable.class).newInstance(from);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
