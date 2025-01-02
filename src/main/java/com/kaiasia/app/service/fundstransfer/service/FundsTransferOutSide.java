package com.kaiasia.app.service.fundstransfer.service;

import com.fasterxml.jackson.core.JacksonException;
import com.kaiasia.app.core.model.*;
import com.kaiasia.app.core.utils.GetErrorUtils;
import com.kaiasia.app.register.KaiMethod;
import com.kaiasia.app.register.KaiService;
import com.kaiasia.app.register.Register;
import com.kaiasia.app.service.fundstransfer.configuration.DepApiConfig;
import com.kaiasia.app.service.fundstransfer.configuration.DepApiProperties;
import com.kaiasia.app.service.fundstransfer.configuration.KaiApiRequestBuilderFactory;
import com.kaiasia.app.service.fundstransfer.dao.ITransactionInfoDAO;
import com.kaiasia.app.service.fundstransfer.exception.ExceptionHandler;
import com.kaiasia.app.service.fundstransfer.exception.InsertFailedException;
import com.kaiasia.app.service.fundstransfer.exception.UpdateFailedException;
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
    @Autowired
    private ExceptionHandler exceptionHandler;

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
        HashMap requestTransaction = (HashMap) request.getBody().get("transaction");
        String location = "FundsTransferOutSide/" + requestTransaction.get("sessionId") + "/" + System.currentTimeMillis();
        return exceptionHandler.handle(req -> {
            ApiResponse response = new ApiResponse();
            ApiError error;
            ApiHeader header = request.getHeader();
            header.setReqType("RESPONSE");
            response.setHeader(header);

            // Call Auth-1 Check Session
            DepApiProperties authApiProperties = depApiConfig.getAuthApi();
            HashMap<String, Object> auth1RequestEnquiry = new HashMap<>();
            auth1RequestEnquiry.put("authenType", "takeSession");
            auth1RequestEnquiry.put("sessionId", requestTransaction.get("sessionId"));
            ApiRequest auth1Request = kaiApiRequestBuilderFactory.getBuilder()
                    .api(authApiProperties.getApiName())
                    .apiKey(authApiProperties.getApiKey())
                    .bodyProperties("command", "GET_ENQUIRY")
                    .bodyProperties("enquiry", auth1RequestEnquiry)
                    .build();

            String username = "";
            ApiResponse auth1Response = ApiCallHelper.call(authApiProperties.getUrl(), HttpMethod.POST, ObjectAndJsonUtils.toJson(auth1Request), ApiResponse.class);
            error = auth1Response.getError();
            if (error != null || !"OK".equals(auth1Response.getBody().get("status"))) {
                log.error("{}#{}", location + "#After call Auth-1", error);
                response.setError(error);
                return response;
            }
            username = (String) ((HashMap) auth1Response.getBody().get("enquiry")).get("username");

            // Call Auth-3 Confirm OTP
            HashMap<String, Object> auth3RequestEnquiry = new HashMap<>();
            auth3RequestEnquiry.put("authenType", "confirmOTP");
            auth3RequestEnquiry.put("sessionId", requestTransaction.get("sessionId"));
            auth3RequestEnquiry.put("username", username);
            auth3RequestEnquiry.put("otp", requestTransaction.get("OTP"));
            auth3RequestEnquiry.put("transTime", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
            auth3RequestEnquiry.put("transId", requestTransaction.get("transactionId"));
            ApiRequest auth3Request = kaiApiRequestBuilderFactory.getBuilder()
                    .api(authApiProperties.getApiName())
                    .apiKey(authApiProperties.getApiKey())
                    .bodyProperties("command", "GET_ENQUIRY")
                    .bodyProperties("enquiry", auth3RequestEnquiry)
                    .build();

            ApiResponse auth3Response = ApiCallHelper.call(authApiProperties.getUrl(), HttpMethod.POST, ObjectAndJsonUtils.toJson(auth3Request), ApiResponse.class);
            error = auth3Response.getError();
            if (error != null || !"OK".equals(auth3Response.getBody().get("status"))) {
                log.error("{}:{}", location + "#After call Auth-3", error);
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
                throw new InsertFailedException(e);
            }

            //Call T2405 - Funds transfer logic
            DepApiProperties t24utilsApiProperties = depApiConfig.getT24utilsApi();
            HashMap<String, Object> t2405RequestTransaction = new HashMap<>();
            t2405RequestTransaction.put("authenType", "fundTransfer");
            t2405RequestTransaction.put("transactionId", requestTransaction.get("transactionId"));
            t2405RequestTransaction.put("debitAccount", requestTransaction.get("debitAccount"));
            t2405RequestTransaction.put("creditAccount", requestTransaction.get("creditAccount"));
            t2405RequestTransaction.put("bankId", requestTransaction.get("bankId"));
            t2405RequestTransaction.put("transAmount", requestTransaction.get("transAmount"));
            t2405RequestTransaction.put("transDesc", requestTransaction.get("transDesc"));
            ApiRequest t2405Request = kaiApiRequestBuilderFactory.getBuilder()
                    .api(t24utilsApiProperties.getApiName())
                    .apiKey(t24utilsApiProperties.getApiKey())
                    .bodyProperties("command", "GET_TRANSACTION")
                    .bodyProperties("transaction", t2405RequestTransaction)
                    .build();

            ApiResponse t2405Response = ApiCallHelper.call(t24utilsApiProperties.getUrl(), HttpMethod.POST, ObjectAndJsonUtils.toJson(t2405Request), ApiResponse.class);
            error = t2405Response.getError();
            if (error != null || !"OK".equals(t2405Response.getBody().get("status"))) {
                log.error("{}:{}", location + "#After call T2405", error);
                response.setError(error);
                return response;
            }

            HashMap t2405ResponseTransaction = (HashMap) t2405Response.getBody().get("transaction");
            HashMap<String, Object> params = new HashMap();
            params.put("response_code", t2405ResponseTransaction.get("responseCode"));
            params.put("bank_trans_id", t2405ResponseTransaction.get("FT"));
            params.put("last_update", new Date());

            try {
                transactionInfoDAO.update(transactionInfo.getTransactionId(), params);
            } catch (Exception e) {
                throw new UpdateFailedException(e);
            }

            //Call NAPAS-2
            DepApiProperties napasApiProperties = depApiConfig.getNapasApi();
            HashMap<String, Object> napas2RequestTransaction = new HashMap<>();
            napas2RequestTransaction.put("authenType", "getTransFastAcc");
            napas2RequestTransaction.put("senderAccount", requestTransaction.get("debitAccount")); // chua ro rang du lieu
            napas2RequestTransaction.put("amount", requestTransaction.get("transAmount"));
            napas2RequestTransaction.put("ccy", "VND");
            napas2RequestTransaction.put("transRef", "VND"); // chua ro rang du lieu
            napas2RequestTransaction.put("benAcc", requestTransaction.get("creditAccount")); // chua ro rang du lieu
            napas2RequestTransaction.put("bankId", requestTransaction.get("bankId"));
            napas2RequestTransaction.put("transContent", requestTransaction.get("transDesc"));
            ApiRequest napas2Request = kaiApiRequestBuilderFactory.getBuilder()
                    .api(napasApiProperties.getApiName())
                    .apiKey(napasApiProperties.getApiKey())
                    .bodyProperties("command", "GET_TRANSACTION")
                    .bodyProperties("enquiry", napas2RequestTransaction)
                    .build();

            ApiResponse napas2Response = ApiCallHelper.call(napasApiProperties.getUrl(), HttpMethod.POST, ObjectAndJsonUtils.toJson(napas2Request), ApiResponse.class);
            error = napas2Response.getError();
            if (error != null || !"OK".equals(napas2Response.getBody().get("status"))) {
                log.error("{}:{}", location + "#After call Napas2", error);
                // TODO: revert giao dich
            }


            // build success body
            ApiBody body = new ApiBody();
            HashMap<String, Object> responseTransaction = new HashMap<>();
            responseTransaction.put("responseCode", "00");
            responseTransaction.put("transactionNO", t2405ResponseTransaction.get("FT"));
            responseTransaction.put("napasRef", ((HashMap) napas2Response.getBody().get("transaction")).get("napasRef"));
            body.put("transaction", responseTransaction);
            response.setBody(body);
            return response;
        }, request, "FundsTransferOutSide/" + requestTransaction.get("sessionId") + "/" + System.currentTimeMillis());
    }
}
