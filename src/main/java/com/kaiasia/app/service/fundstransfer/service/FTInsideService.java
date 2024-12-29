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
import com.kaiasia.app.service.fundstransfer.model.FundsTransferIn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Field;
import java.util.stream.Collectors;

@KaiService
@Slf4j
public class FTInsideService {

    @Autowired
    GetErrorUtils apiErrorUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @KaiMethod(name = "FTInsideService", type = Register.VALIDATE)
    public ApiError validate(ApiRequest req) {
        try {
            ApiBody body = req.getBody();
            if (body == null) {
                return apiErrorUtils.getError("804", new String[]{"Missing request body"});
            }

            if(body.get("transaction") == null) {
                return apiErrorUtils.getError("804", new String[]{"Transaction part is required"});
            }

            FundsTransferIn input = objectMapper.convertValue(body.get("transaction"), FundsTransferIn.class);

            List<String> missingFields = Arrays.stream(input.getClass().getDeclaredFields())
                                               .peek(field -> field.setAccessible(true))
                                               .filter(field -> {
                                                   try {
                                                       return field.get(input) == null;
                                                   } catch (IllegalAccessException e) {
                                                       throw new RuntimeException(e);
                                                   }
                                               })
                                               .map(Field::getName)
                                               .collect(Collectors.toList());

            if (!missingFields.isEmpty()) {
                return apiErrorUtils.getError("804",
                        new String[]{"Missing mandatory fields: " + String.join(", ", missingFields)});
            }

            return new ApiError(ApiError.OK_CODE, ApiError.OK_DESC);
        } catch (IllegalArgumentException e) {
            return apiErrorUtils.getError("600", new String[]{"Invalid request body format"});
        }
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
