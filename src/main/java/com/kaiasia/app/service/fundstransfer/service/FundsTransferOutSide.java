package com.kaiasia.app.service.fundstransfer.service;

import com.kaiasia.app.core.model.*;
import com.kaiasia.app.core.utils.GetErrorUtils;
import com.kaiasia.app.register.KaiMethod;
import com.kaiasia.app.register.KaiService;
import com.kaiasia.app.register.Register;
import com.kaiasia.app.service.fundstransfer.configuration.DepApiConfig;
import com.kaiasia.app.service.fundstransfer.configuration.DepApiProperties;
import com.kaiasia.app.service.fundstransfer.configuration.KaiApiRequestBuilderFactory;
import com.kaiasia.app.service.fundstransfer.dao.ITransactionInfoDAO;
import com.kaiasia.app.service.fundstransfer.dao.impl.TransactionInfoDAO;
import com.kaiasia.app.service.fundstransfer.model.TransactionInfo;
import com.kaiasia.app.service.fundstransfer.utils.ApiCallHelper;
import com.kaiasia.app.service.fundstransfer.utils.ObjectAndJsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

@KaiService
@Slf4j
public class FundsTransferOutSide {
    @Autowired
    private GetErrorUtils getErrorUtils;
    @Autowired
    private DepApiConfig depApiConfig;
    @Autowired
    private KaiApiRequestBuilderFactory kaiApiRequestBuilderFactory;
    @Autowired
    private ITransactionInfoDAO transactionInfoDAO;

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
        String location = "FundsTransferOutSide/" + requestTransaction.get("sessionId") + "/" + System.currentTimeMillis();

        // Call Auth-1 Check Session
        DepApiProperties authApiProperties = depApiConfig.getAuthApi();
        HashMap<String, Object> auth1Enquiry = new HashMap<>();
        auth1Enquiry.put("authenType", "takeSession");
        auth1Enquiry.put("sessionId", requestTransaction.get("sessionId"));
        ApiRequest auth1Request = kaiApiRequestBuilderFactory.getBuilder()
                .api(authApiProperties.getApiName())
                .apiKey(authApiProperties.getApiKey())
                .bodyProperties("command", "GET_ENQUIRY")
                .bodyProperties("enquiry", auth1Enquiry)
                .build();

        String username = "";
        try {
            ApiResponse auth1Response = ApiCallHelper.call(authApiProperties.getUrl(), HttpMethod.POST, ObjectAndJsonUtils.toJson(auth1Request), ApiResponse.class);
            error = auth1Response.getError();
            if (error != null || !"OK".equals(auth1Response.getBody().get("status"))) {
                log.error("{}:{}", location + "#After call Auth-1", error);
                response.setError(error);
                return response;
            }
            username = (String) ((HashMap) auth1Response.getBody().get("enquiry")).get("username");
        } catch (Exception e) {
            log.error("{}:{}", location + "#Calling Auth-1", e.getMessage());
            error = getErrorUtils.getError("999", new String[]{e.getMessage()});
            response.setError(error);
            return response;
        }


        // Call Auth-3 Confirm OTP
        HashMap<String, Object> auth3Enquiry = new HashMap<>();
        auth3Enquiry.put("authenType", "confirmOTP");
        auth3Enquiry.put("sessionId", requestTransaction.get("sessionId"));
        auth3Enquiry.put("username", username);
        auth3Enquiry.put("otp", requestTransaction.get("OTP"));
        auth3Enquiry.put("transTime", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        auth3Enquiry.put("transId", requestTransaction.get("transactionId"));
        ApiRequest auth3Request = kaiApiRequestBuilderFactory.getBuilder()
                .api(authApiProperties.getApiName())
                .apiKey(authApiProperties.getApiKey())
                .bodyProperties("command", "GET_ENQUIRY")
                .bodyProperties("enquiry", auth3Enquiry)
                .build();

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

        String customerId = (String) requestTransaction.get("customerID");
        TransactionInfo transactionInfo = TransactionInfo.builder()
                .transactionId(String.join("-", customerId, new SimpleDateFormat("ddMMyyyy").format(new Date())))
                .customerId(customerId)
                .otp((String) requestTransaction.get("OTP"))
                .approvalMethod("SOFTOTP")
                .insertTime(new Date())
                .status("PROCESSING")
                .build();
        try {
            transactionInfoDAO.insert(transactionInfo);
        } catch (Exception e) {
            log.error("{}#Failed to insert transaction {} to database:{}", location, transactionInfo, e.getMessage());
            error = getErrorUtils.getError("500", new String[]{e.getMessage()});
            response.setError(error);
            return response;
        }

        //Call T2405 - Funds transfer logic
        DepApiProperties t24utilsApiProperties = depApiConfig.getT24utilsApi();
        HashMap<String, Object> t2405Transaction = new HashMap<>();
        t2405Transaction.put("authenType", "fundTransfer");
        t2405Transaction.put("transactionId", requestTransaction.get("transactionId"));
        t2405Transaction.put("debitAccount", requestTransaction.get("debitAccount"));
        t2405Transaction.put("creditAccount", requestTransaction.get("creditAccount"));
        t2405Transaction.put("bankId", requestTransaction.get("bankId"));
        t2405Transaction.put("transAmount", requestTransaction.get("transAmount"));
        t2405Transaction.put("transDesc", requestTransaction.get("transDesc"));
        ApiRequest t2405Request = kaiApiRequestBuilderFactory.getBuilder()
                .api(t24utilsApiProperties.getApiName())
                .apiKey(t24utilsApiProperties.getApiKey())
                .bodyProperties("command", "GET_TRANSACTION")
                .bodyProperties("enquiry", t2405Transaction)
                .build();

        try {
            ApiResponse t2405Response = ApiCallHelper.call(t24utilsApiProperties.getUrl(), HttpMethod.POST, ObjectAndJsonUtils.toJson(t2405Request), ApiResponse.class);
            error = t2405Response.getError();
            if (error != null || !"OK".equals(t2405Response.getBody().get("status"))) {
                log.error("{}:{}", location + "#After call T2405", error);
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
        HashMap<String, Object> napas2Transaction = new HashMap<>();
        napas2Transaction.put("authenType", "getTransFastAcc");
        napas2Transaction.put("senderAccount", requestTransaction.get("debitAccount")); // chua ro rang du lieu
        napas2Transaction.put("amount", requestTransaction.get("transAmount"));
        napas2Transaction.put("ccy", "VND");
        napas2Transaction.put("transRef", "VND"); // chua ro rang du lieu
        napas2Transaction.put("benAcc", requestTransaction.get("creditAccount")); // chua ro rang du lieu
        napas2Transaction.put("bankId", requestTransaction.get("bankId"));
        napas2Transaction.put("transContent", requestTransaction.get("transDesc"));
        ApiRequest napas2Request = kaiApiRequestBuilderFactory.getBuilder()
                .api(napasApiProperties.getApiName())
                .apiKey(napasApiProperties.getApiKey())
                .bodyProperties("command", "GET_TRANSACTION")
                .bodyProperties("enquiry", napas2Transaction)
                .build();

        try {
            ApiResponse napas2Response = ApiCallHelper.call(napasApiProperties.getUrl(), HttpMethod.POST, ObjectAndJsonUtils.toJson(napas2Request), ApiResponse.class);
            error = napas2Response.getError();
            if (error != null || !"OK".equals(napas2Response.getBody().get("status"))) {
                log.error("{}:{}", location + "#After call Napas2", error);
                // TODO: revert giao dich
            }
        } catch (Exception e) {
            log.error("{}:{}", location + "#Calling Napas2", e.getMessage());
            error = getErrorUtils.getError("999", new String[]{e.getMessage()});
            response.setError(error);
            return response;
        }

        return response;
    }
}
