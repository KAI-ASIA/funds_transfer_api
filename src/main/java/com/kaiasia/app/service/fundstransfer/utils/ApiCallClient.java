package com.kaiasia.app.service.fundstransfer.utils;

import com.kaiasia.app.core.model.*;
import com.kaiasia.app.core.utils.ApiConstant;
import com.kaiasia.app.service.fundstransfer.configuration.KaiRestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;


@Component
@Slf4j
@Setter
@ToString
public class ApiCallClient {
    @Autowired
    private KaiRestTemplate kaiRestTemplate;
    private String url;
    private String apiKey;
    private String apiName;
    private int timeout;
    private Map<String, String> authenType;

    public <T> T call(String location, ApiRequest request, Class<T> responseType) {
        request.setHeader(rebuildHeader(request.getHeader()));
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

    private ApiHeader rebuildHeader(ApiHeader header) {
        header.setApi(apiName);
        header.setApiKey(apiKey);
        return header;
    }
}
