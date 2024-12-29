package com.kaiasia.app.service.fundstransfer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaiasia.app.core.model.ApiBody;
import com.kaiasia.app.core.model.ApiError;
import com.kaiasia.app.core.model.ApiRequest;
import com.kaiasia.app.core.model.ApiResponse;
import com.kaiasia.app.core.utils.GetErrorUtils;
import com.kaiasia.app.register.KaiMethod;
import com.kaiasia.app.register.KaiService;
import com.kaiasia.app.register.Register;
import com.kaiasia.app.service.fundstransfer.utils.ServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@KaiService
@Slf4j
public class FTInsideService {

    @Autowired
    GetErrorUtils apiErrorUtils;

    @KaiMethod(name = "FTInsideService", type = Register.VALIDATE)
    public ApiError validate(ApiRequest req) {
        return ServiceUtils.validate(req, FTInsideService.class, apiErrorUtils);
    }

    @KaiMethod(name = "FTInsideService")
    public ApiResponse process(ApiRequest req){
        ApiResponse apiResponse = new ApiResponse();
        ApiBody apiBody = new ApiBody();
        try {
            Map<String, Object> bodyEnq = new HashMap<>();

            apiResponse.setBody(apiBody);
            return apiResponse;
        } catch (Exception e){
            log.error("Exception", e);
            ApiError apiError = apiErrorUtils.getError("999", new String[]{e.getMessage()});
            apiResponse.setError(apiError);
            return apiResponse;
        }

    }

}
