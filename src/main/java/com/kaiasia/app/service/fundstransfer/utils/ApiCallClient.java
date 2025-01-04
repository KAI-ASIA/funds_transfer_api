package com.kaiasia.app.service.fundstransfer.utils;

import com.kaiasia.app.core.job.BaseService;
import com.kaiasia.app.core.model.*;
import com.kaiasia.app.core.utils.ApiConstant;
import com.kaiasia.app.service.Auth_api.utils.ModelMapper;
import com.kaiasia.app.service.fundstransfer.configuration.KaiRestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@Setter
public class ApiCallClient {
    @Autowired
    private KaiRestTemplate kaiRestTemplate;
    private String url;
    private String apiKey;
    private String apiName;
    private int timeout;

    public <T> T call(String location, ApiRequest request, Class<T> responseType) {
        request.setHeader(rebuildHeader(request.getHeader()));
        log.info("{}#begin call api {}", location, apiName);
        ApiResponse response = kaiRestTemplate.call(url, request, timeout);
        log.info("{}#end call api {}", location, apiName);
//        if (response.getError() != null || ApiError.OK_CODE.equals(response.getBody().get("status"))) {
//            return ObjectAndJsonUtils.fromObject(response.getError(), responseType);
//        }
        ApiError apiError = new ApiError();
        if (response != null && response.getError() != null) {
            apiError = response.getError();
            ModelMapper mapper = new ModelMapper();
            return mapper.map(apiError, classResult);
        }
        
        Map<String, Object> enquiryMap = BaseService.getEnquiry(response);
        if(enquiryMap==null){
        	enquiryMap = BaseService.getTransaction(response);
        }
        ModelMapper mapper = new ModelMapper();
         
        return mapper.map(enquiryMap, classResult);
        
        
//        return getResponseTranOrEnq(response, responseType);
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
