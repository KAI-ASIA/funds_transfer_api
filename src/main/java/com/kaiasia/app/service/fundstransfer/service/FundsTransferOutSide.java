package com.kaiasia.app.service.fundstransfer.service;

import com.kaiasia.app.core.model.*;
import com.kaiasia.app.core.utils.GetErrorUtils;
import com.kaiasia.app.register.KaiMethod;
import com.kaiasia.app.register.KaiService;
import com.kaiasia.app.register.Register;
import com.kaiasia.app.service.fundstransfer.configuration.DepApiConfig;
import com.kaiasia.app.service.fundstransfer.configuration.DepApiProperties;
import com.kaiasia.app.service.fundstransfer.utils.ApiCallHelper;
import com.kaiasia.app.service.fundstransfer.utils.ObjectAndJsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.util.HashMap;

@KaiService
@Slf4j
public class FundsTransferOutSide {
    @Autowired
    private GetErrorUtils getErrorUtils;
    @Autowired
    private DepApiConfig depApiConfig;

    @KaiMethod(name = "FundsTransferOutSide", type = Register.VALIDATE)
    public ApiError validate(ApiRequest request) {
        ApiError error = new ApiError(ApiError.OK_CODE, ApiError.OK_DESC);
        HashMap transaction = (HashMap) request.getBody().get("transaction");

        if (transaction == null) {
            error = getErrorUtils.getError("804", new String[]{"transaction part is required"});
            return error;
        }

        String[] requiredFields = new String[]{
                "sessionId", "customerID", "company",
                "OTP", "transactionId", "debitAccount",
                "creditAccount", "bankId", "transAmount",
                "transDesc"
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
        ApiError error;
        ApiHeader header = request.getHeader();
        header.setReqType("RESPONSE");
        response.setHeader(header);

        HashMap requestTransaction = (HashMap) request.getBody().get("transaction");
        String location = "FundsTransferOutSide/" + requestTransaction.get("sessionId") + "/" + requestTransaction.get("customerID");

        // Call Auth-1 Check Session
        DepApiProperties authApiProperties = depApiConfig.getAuthApi();
        ApiRequest auth1Request = new ApiRequest();
        ApiHeader auth1Header = new ApiHeader();
        auth1Header.setReqType("REQUEST");
        auth1Header.setApi(authApiProperties.getApiName());
        auth1Header.setApiKey(authApiProperties.getApiKey());
        auth1Header.setPriority(1);
        auth1Header.setChannel("API");
        auth1Header.setLocation("PC");
        auth1Header.setRequestAPI("FUNDS_TRANSFER_API");
        auth1Request.setHeader(header);
        ApiBody auth1Body = new ApiBody();
        auth1Body.put("command", "GET_ENQUIRY");
        HashMap<String, Object> auth1Enquiry = new HashMap<>();
        auth1Enquiry.put("authenType", "takeSession");
        auth1Enquiry.put("sessionId", requestTransaction.get("sessionId"));
        auth1Body.put("enquiry", auth1Enquiry);
        auth1Request.setBody(auth1Body);


        try {
            ApiResponse auth1Response = ApiCallHelper.call(authApiProperties.getUrl(), HttpMethod.POST, ObjectAndJsonUtils.toJson(auth1Request), ApiResponse.class);
            error = auth1Response.getError();
            if (error != null || !"OK".equals(auth1Response.getBody().get("status"))) {
                log.error("{}:{}", location + "#After call Auth-1", error);
                response.setError(error);
                return response;
            }
        } catch (Exception e) {
            log.error("{}:{}", location + "#Calling Auth-1", e.getMessage());
            error = getErrorUtils.getError("999", new String[]{e.getMessage()});
            response.setError(error);
            return response;
        }


        // Call Auth-3 Confirm OTP
        ApiRequest auth3Request = new ApiRequest();
        auth3Request.setHeader(auth1Header);
        ApiBody auth3Body = new ApiBody();
        auth3Body.put("command", "GET_ENQUIRY");
        HashMap<String, Object> auth3Enquiry = new HashMap<>();
        auth1Enquiry.put("authenType", "confirmOTP");
        auth1Enquiry.put("sessionId", requestTransaction.get("sessionId"));
        auth1Enquiry.put("username", requestTransaction.get("customerID")); // du lieu chua ro rang
        auth1Enquiry.put("otp", requestTransaction.get("OTP"));
        auth1Enquiry.put("transTime", requestTransaction.get("sessionId"));
        auth1Enquiry.put("transId", requestTransaction.get("transactionId"));
        auth3Body.put("enquiry", auth3Enquiry);
        auth3Request.setBody(auth3Body);

        try {
            ApiResponse auth3Response = ApiCallHelper.call(authApiProperties.getUrl(), HttpMethod.POST, ObjectAndJsonUtils.toJson(auth3Request), ApiResponse.class);
            error = auth3Response.getError();
            if (error != null || !"OK".equals(auth3Response.getBody().get("status"))) {
                log.error("{}:{}", location + "#After call Auth-3", error);
                response.setError(error);
                return response;
            }
        } catch (Exception e) {
            log.error("{}:{}", location + "#Calling Auth-3", e.getMessage());
            error = getErrorUtils.getError("999", new String[]{e.getMessage()});
            response.setError(error);
            return response;
        }


        //Call T2405 - Funds transfer logic
        DepApiProperties t24utilsApiProperties = depApiConfig.getT24utilsApi();
        ApiRequest t2405Request = new ApiRequest();
        ApiHeader t2405Header = new ApiHeader();
        t2405Header.setReqType("REQUEST");
        t2405Header.setApi(t24utilsApiProperties.getApiName());
        t2405Header.setApiKey(t24utilsApiProperties.getApiKey());
        t2405Header.setPriority(1);
        t2405Header.setChannel("API");
        t2405Header.setLocation("PC");
        t2405Header.setRequestAPI("FUNDS_TRANSFER_API");
        t2405Request.setHeader(t2405Header);
        ApiBody t2405Body = new ApiBody();
        t2405Body.put("command", "GET_TRANSACTION");
        HashMap<String, Object> t2405Transaction = new HashMap<>();
        t2405Transaction.put("authenType", "fundTransfer");
        t2405Transaction.put("transactionId", requestTransaction.get("transactionId"));
        t2405Transaction.put("debitAccount", requestTransaction.get("debitAccount"));
        t2405Transaction.put("creditAccount", requestTransaction.get("creditAccount"));
        t2405Transaction.put("bankId", requestTransaction.get("bankId"));
        t2405Transaction.put("transAmount", requestTransaction.get("transAmount"));
        t2405Transaction.put("transDesc", requestTransaction.get("transDesc"));
        t2405Request.setBody(t2405Body);

        try {
            ApiResponse t2405Response = ApiCallHelper.call(t24utilsApiProperties.getUrl(), HttpMethod.POST, ObjectAndJsonUtils.toJson(t2405Request), ApiResponse.class);
            error = t2405Response.getError();
            if (error != null || !"OK".equals(t2405Response.getBody().get("status"))) {
                log.error("{}:{}", location + "#After call Auth-2", error);
                response.setError(error);
                return response;
            }
        } catch (Exception e) {
            log.error("{}:{}", location + "#Calling T2405", e.getMessage());
            error = getErrorUtils.getError("999", new String[]{e.getMessage()});
            response.setError(error);
            return response;
        }

        //Call NAPAS-2
        DepApiProperties napasApiProperties = depApiConfig.getNapasApi();
        ApiRequest napas2Request = new ApiRequest();
        ApiHeader napasHeader = new ApiHeader();
        napasHeader.setReqType("REQUEST");
        napasHeader.setApi(napasApiProperties.getApiName());
        napasHeader.setApiKey(napasApiProperties.getApiKey());
        napasHeader.setPriority(1);
        napasHeader.setChannel("API");
        napasHeader.setLocation("PC");
        napasHeader.setRequestAPI("FUNDS_TRANSFER_API");
        napas2Request.setHeader(napasHeader);
        ApiBody napasBody = new ApiBody();
        napasBody.put("command", "GET_TRANSACTION");
        HashMap<String, Object> napasTransaction = new HashMap<>();
        napasTransaction.put("authenType", "getTransFastAcc");
        napasTransaction.put("senderAccount", requestTransaction.get("debitAccount")); // chua ro rang du lieu
        napasTransaction.put("amount", requestTransaction.get("transAmount"));
        napasTransaction.put("ccy", "VND");
        napasTransaction.put("transRef", "VND"); // chua ro rang du lieu
        napasTransaction.put("benAcc", requestTransaction.get("creditAccount")); // chua ro rang du lieu
        napasTransaction.put("bankId", requestTransaction.get("bankId"));
        napasTransaction.put("transContent", requestTransaction.get("transDesc"));
        napasBody.put("transaction", napasTransaction);
        napas2Request.setBody(napasBody);

        try {
            ApiResponse napas2Response = ApiCallHelper.call(napasApiProperties.getUrl(), HttpMethod.POST, ObjectAndJsonUtils.toJson(napas2Request), ApiResponse.class);
            error = napas2Response.getError();
            if (error != null || !"OK".equals(napas2Response.getBody().get("status"))) {
                log.error("{}:{}", location + "#After call Auth-2", error);
                // revert giao dich
            }
        } catch (Exception e) {
            log.error("{}:{}", location + "#Calling T2405", e.getMessage());
            error = getErrorUtils.getError("999", new String[]{e.getMessage()});
            response.setError(error);
            return response;
        }

        return response;
    }
}
