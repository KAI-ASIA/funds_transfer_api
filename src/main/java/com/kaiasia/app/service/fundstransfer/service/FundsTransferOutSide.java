package com.kaiasia.app.service.fundstransfer.service;

import com.kaiasia.app.core.model.*;
import com.kaiasia.app.core.utils.GetErrorUtils;
import com.kaiasia.app.register.KaiMethod;
import com.kaiasia.app.register.KaiService;
import com.kaiasia.app.register.Register;
import com.kaiasia.app.service.fundstransfer.utils.ApiCallHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;

@KaiService
public class FundsTransferOutSide {
    @Autowired
    private GetErrorUtils getErrorUtils;

    @KaiMethod(name = "FundsTransferOutSide", type = Register.VALIDATE)
    public ApiError validate(ApiRequest request) {
        ApiError error = new ApiError(ApiError.OK_CODE, ApiError.OK_DESC);
        HashMap transaction = (HashMap) request.getBody().get("transaction");

        if (transaction == null) {
            error = getErrorUtils.getError("804", new String[]{"transaction part is required"});
            return error;
        }

        String[] requiredFields = new String[]{
                "sessionId", "customerID", "company", "OTP", "transactionId", "debitAccount", "creditAccount", "bankId", "transAmount", "transDesc"
        };

        for (String requiredField : requiredFields) {
            if (!transaction.containsKey(requiredField) || StringUtils.isEmpty((String) transaction.get(requiredField))) {
                error = getErrorUtils.getError("804", new String[]{requiredField + " is required"});
                return error;
            }
        }

        return error;
    }

    @KaiMethod(name = "FundsTransferOutSide")
    public ApiResponse process(ApiRequest request) {
        ApiResponse response = new ApiResponse();
        ApiHeader header = request.getHeader();
        header.setReqType("RESPONSE");
        response.setHeader(header);

        ApiBody body = new ApiBody();

        return response;
    }
}
