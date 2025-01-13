package com.kaiasia.app.service.fundstransfer.exception;

import com.kaiasia.app.core.utils.GetErrorUtils;
import lombok.extern.slf4j.Slf4j;
import ms.apiclient.model.ApiError;
import ms.apiclient.model.ApiHeader;
import ms.apiclient.model.ApiRequest;
import ms.apiclient.model.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

@Component
@Slf4j
public class ExceptionHandler {
    @Autowired
    private GetErrorUtils getErrorUtils;

    /**
     * @param process    hàm logic xử lý mà ta muốn ủy quyền lại cho {@link ExceptionHandler}
     * @param apiRequest tham số mà hàm <b>process</b> cần
     * @param location   vị trí để thực thi ghi log lỗi
     * @return trả về response tương ứng vứi hàm process trả về
     */
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
            error = getErrorUtils.getError(ApiErrorCode.FAILED_TO_INSERT_TO_DB, new String[]{e.getMessage()});
            return error;
        }
        if (e instanceof UpdateFailedException) {
            error = getErrorUtils.getError(ApiErrorCode.FAILED_TO_UPDATE_TO_DB, new String[]{e.getMessage()});
            return error;
        }
        if (e instanceof RestClientException) {
            error = getErrorUtils.getError(ApiErrorCode.FAILED_TO_CALL_API, new String[]{e.getMessage()});
            return error;
        }
        if (e instanceof MapperException) {
            error = getErrorUtils.getError("600", new String[]{e.getMessage()});
            return error;
        }
        error = getErrorUtils.getError(ApiErrorCode.INTERNAL_ERROR, new String[]{e.getMessage()});
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
