package com.kaiasia.app.service.fundstransfer.utils;

import com.kaiasia.app.core.model.ApiBody;
import com.kaiasia.app.core.model.ApiError;
import com.kaiasia.app.core.model.ApiRequest;
import com.kaiasia.app.core.model.ApiResponse;
import com.kaiasia.app.core.utils.ApiConstant;
import com.kaiasia.app.service.fundstransfer.configuration.KaiRestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class ApiCallClient {
    @Autowired
    private KaiRestTemplate kaiRestTemplate;
    private String url;
    private String apiKey;
    private String apiName;
    private int timeout;

    public <T> T call(String location, ApiRequest request, Class<T> responseType) {
        log.info("{}#begin call api {}", location, apiName);
        ApiResponse response = kaiRestTemplate.call(url, request, timeout);
        log.info("{}#end call api {}", location, apiName);
        if (response.getError() != null || ApiError.OK_CODE.equals(response.getBody().get("status"))) {
            return ObjectAndJsonUtils.fromObject(response.getError(), responseType);
        }
        return getResponseTranOrEnq(response, responseType);
    }

    private static <T> T getResponseTranOrEnq(ApiResponse response, Class<T> responseType) {
        ApiBody body = response.getBody();
        if (body.containsKey(ApiConstant.COMMAND.TRANSACTION)) {
            return ObjectAndJsonUtils.fromJson((String) body.get(ApiConstant.COMMAND.TRANSACTION), responseType);
        }
        return ObjectAndJsonUtils.fromJson((String) body.get(ApiConstant.COMMAND.ENQUIRY), responseType);
    }
}
