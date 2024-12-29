package com.kaiasia.app.service.fundstransfer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaiasia.app.core.model.*;
import com.kaiasia.app.core.utils.GetErrorUtils;
import com.kaiasia.app.register.KaiMethod;
import com.kaiasia.app.register.KaiService;
import com.kaiasia.app.register.Register;
import com.kaiasia.app.service.fundstransfer.configuration.DepApiConfig;
import com.kaiasia.app.service.fundstransfer.configuration.DepApiProperties;
import com.kaiasia.app.service.fundstransfer.model.FundsTransferIn;
import com.kaiasia.app.service.fundstransfer.utils.ApiCallHelper;
import com.kaiasia.app.service.fundstransfer.utils.ObjectAndJsonUtils;
import com.kaiasia.app.service.fundstransfer.utils.ServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@KaiService
@Slf4j
public class FTInsideService {

    @Autowired
    GetErrorUtils apiErrorUtils;

    @Autowired
    private DepApiConfig depApiConfig;

    @KaiMethod(name = "FTInsideService", type = Register.VALIDATE)
    public ApiError validate(ApiRequest req) {
        return ServiceUtils.validate(req, FundsTransferIn.class, apiErrorUtils);
    }

    @KaiMethod(name = "FTInsideService")
    public ApiResponse process(ApiRequest req) {
        ApiResponse response = new ApiResponse();
        ApiHeader header = req.getHeader();
        header.setReqType("RESPONSE");
        response.setHeader(header);

        ApiBody body = new ApiBody();
        response.setBody(body);
        return response;
    }
}
