package com.kaiasia.app.service.fundstransfer.service;

import com.kaiasia.app.core.model.*;
import com.kaiasia.app.register.KaiMethod;
import com.kaiasia.app.register.KaiService;
import com.kaiasia.app.register.Register;
import com.kaiasia.app.service.fundstransfer.utils.ApiCallHelper;

@KaiService
public class FundsTransferOutSide {

    @KaiMethod(name = "FundsTransferOutSide",type = Register.VALIDATE)
    public ApiError validate(ApiRequest request){
        ApiError error = new ApiError(ApiError.OK_CODE,ApiError.OK_DESC);

        return error;
    }

    @KaiMethod(name = "FundsTransferOutSide")
    public ApiResponse process(ApiRequest request){
        ApiResponse response = new ApiResponse();
        response.setHeader(new ApiHeader());
        response.setBody(new ApiBody());
        return response;
    }
}
