package com.kaiasia.app.service.fundstransfer.service;

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
import com.kaiasia.app.service.fundstransfer.model.request.Auth1In;
import com.kaiasia.app.service.fundstransfer.model.request.Auth3In;
import com.kaiasia.app.service.fundstransfer.model.request.FundsTransferIn;
import com.kaiasia.app.service.fundstransfer.model.request.Napas2In;
import com.kaiasia.app.service.fundstransfer.utils.ApiCallHelper;
import com.kaiasia.app.service.fundstransfer.utils.ObjectAndJsonUtils;
import lombok.extern.slf4j.Slf4j;
import ms.apiclient.model.*;
import ms.apiclient.t24util.T24UtilClient;
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
    @Autowired
    private T24UtilClient t24UtilClient;


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
            Auth1In auth1RequestEnquiry = Auth1In.builder().authenType("takeSession").sessionId((String) requestTransaction.get("sessionId")).build();
            ApiRequest auth1Request = kaiApiRequestBuilderFactory.getBuilder()
                    .api(authApiProperties.getApiName())
                    .apiKey(authApiProperties.getApiKey())
                    .bodyProperties("command", "GET_ENQUIRY")
                    .bodyProperties("enquiry", auth1RequestEnquiry)
                    .build();

            String username = "";
            ApiResponse auth1Response = ApiCallHelper.call(authApiProperties.getUrl(), HttpMethod.POST, ObjectAndJsonUtils.toJson(auth1Request), ApiResponse.class, authApiProperties.getTimeout());
            error = auth1Response.getError();
            if (error != null || !"OK".equals(auth1Response.getBody().get("status"))) {
                log.error("{}#{}", location + "#After call Auth-1", error);
                response.setError(error);
                return response;
            }
            username = (String) ((HashMap) auth1Response.getBody().get("enquiry")).get("username");

            // Call Auth-3 Confirm OTP
            Auth3In auth3RequestEnquiry = Auth3In.builder().authenType("confirmOTP")
                    .sessionId((String) requestTransaction.get("sessionId"))
                    .username(username).otp((String) requestTransaction.get("OTP"))
                    .transTime(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()))
                    .transId((String) requestTransaction.get("transactionId"))
                    .build();
            ApiRequest auth3Request = kaiApiRequestBuilderFactory.getBuilder()
                    .api(authApiProperties.getApiName())
                    .apiKey(authApiProperties.getApiKey())
                    .bodyProperties("command", "GET_ENQUIRY")
                    .bodyProperties("enquiry", auth3RequestEnquiry)
                    .build();

            ApiResponse auth3Response = ApiCallHelper.call(authApiProperties.getUrl(), HttpMethod.POST, ObjectAndJsonUtils.toJson(auth3Request), ApiResponse.class, authApiProperties.getTimeout());
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
            FundsTransferIn t2405RequestTransaction = FundsTransferIn.builder().authenType("KAI.API.FT.PROCESS")
                    .transactionId((String) requestTransaction.get("transactionId"))
                    .debitAccount((String) requestTransaction.get("debitAccount"))
                    .creditAccount((String) requestTransaction.get("creditAccount"))
                    .bankId((String) requestTransaction.get("bankId"))
                    .transAmount((String) requestTransaction.get("transAmount"))
                    .transDesc((String) requestTransaction.get("transDesc"))
                    .build();
            ApiRequest t2405Request = kaiApiRequestBuilderFactory.getBuilder()
                    .api(t24utilsApiProperties.getApiName())
                    .apiKey(t24utilsApiProperties.getApiKey())
                    .bodyProperties("command", "GET_TRANSACTION")
                    .bodyProperties("transaction", t2405RequestTransaction)
                    .build();

            ApiResponse t2405Response = ApiCallHelper.call(t24utilsApiProperties.getUrl(), HttpMethod.POST, ObjectAndJsonUtils.toJson(t2405Request), ApiResponse.class, t24utilsApiProperties.getTimeout());
            error = t2405Response.getError();
            if (error != null || !"OK".equals(t2405Response.getBody().get("status"))) {
                log.error("{}:{}", location + "#After call T2405", error);
                response.setError(error);
                return response;
            }

            HashMap t2405ResponseTransaction = (HashMap) t2405Response.getBody().get("transaction");
            HashMap<String, Object> params = new HashMap();
            params.put("response_code", t2405ResponseTransaction.get("responseCode"));
            params.put("bank_trans_id", t2405ResponseTransaction.get("transactionNo"));
            params.put("last_update", new Date());

            try {
                transactionInfoDAO.update(transactionInfo.getTransactionId(), params);
            } catch (Exception e) {
                throw new UpdateFailedException(e);
            }

            //Call NAPAS-2
            DepApiProperties napasApiProperties = depApiConfig.getNapasApi();
            Napas2In napas2RequestTransaction = Napas2In.builder().authenType("getTransFastAcc")
                    .senderAccount((String) requestTransaction.get("debitAccount"))// chua ro rang du lieu
                    .amount((String) requestTransaction.get("transAmount")).ccy("VND")
                    .transRef((String) t2405ResponseTransaction.get("transactionNo"))
                    .benAcc((String) requestTransaction.get("creditAccount"))// chua ro rang du lieu
                    .bankId((String) requestTransaction.get("bankId"))
                    .transContent((String) requestTransaction.get("transContent"))
                    .build();
            ApiRequest napas2Request = kaiApiRequestBuilderFactory.getBuilder()
                    .api(napasApiProperties.getApiName())
                    .apiKey(napasApiProperties.getApiKey())
                    .bodyProperties("command", "GET_TRANSACTION")
                    .bodyProperties("enquiry", napas2RequestTransaction)
                    .build();

            ApiResponse napas2Response = ApiCallHelper.call(napasApiProperties.getUrl(), HttpMethod.POST, ObjectAndJsonUtils.toJson(napas2Request), ApiResponse.class, napasApiProperties.getTimeout());
            error = napas2Response.getError();
            if (error != null || !"OK".equals(napas2Response.getBody().get("status"))) {
                log.error("{}:{}", location + "#After call Napas2", error);
                // TODO: revert giao dich
            }


            // build success body
            ApiBody body = new ApiBody();
            HashMap<String, Object> responseTransaction = new HashMap<>();
            responseTransaction.put("responseCode", "00");
            responseTransaction.put("transactionNO", t2405ResponseTransaction.get("transactionNo"));
            responseTransaction.put("napasRef", ((HashMap) napas2Response.getBody().get("transaction")).get("napasRef"));
            body.put("transaction", responseTransaction);
            response.setBody(body);
            return response;
        }, request, "FundsTransferOutSide/" + requestTransaction.get("sessionId") + "/" + System.currentTimeMillis());
    }
}
