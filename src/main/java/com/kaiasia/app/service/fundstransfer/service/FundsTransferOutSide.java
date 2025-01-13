package com.kaiasia.app.service.fundstransfer.service;

import com.kaiasia.app.core.utils.AppConfigPropertiesUtils;
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
import com.kaiasia.app.service.fundstransfer.model.enums.TransactionStatus;
import com.kaiasia.app.service.fundstransfer.model.request.Napas2In;
import com.kaiasia.app.service.fundstransfer.utils.ApiCallHelper;
import com.kaiasia.app.service.fundstransfer.utils.ObjectAndJsonUtils;
import com.kaiasia.app.utils.ApiUtils;
import lombok.extern.slf4j.Slf4j;
import ms.apiclient.authen.AuthOTPResponse;
import ms.apiclient.authen.AuthRequest;
import ms.apiclient.authen.AuthTakeSessionResponse;
import ms.apiclient.authen.AuthenClient;
import ms.apiclient.model.*;
import ms.apiclient.t24util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    @Autowired
    AuthenClient authenClient;
    @Value("${kai.name}")
    private String requestApi;

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
        String location = "FundsTransferOutSide_" + requestTransaction.get("sessionId") + "_" + System.currentTimeMillis();
        log.info("{}#BEGIN", location);
        return exceptionHandler.handle(req -> {
            ApiResponse response = new ApiResponse();
            ApiError error;
            ApiHeader origHeader = request.getHeader();
            ApiHeader header = copyHeaders(origHeader);

            origHeader.setReqType("RESPONSE");
            response.setHeader(origHeader);
            // Call Auth-1 Check Session

            String username = "";
            AuthTakeSessionResponse authTakeSessionResponse = authenClient.takeSession(location, AuthRequest.builder()
                    .sessionId((String) requestTransaction.get("sessionId")).build(), header);
            error = authTakeSessionResponse.getError();
            if (error != null) {
                log.error("{}#{}", location + "#After call Auth-1", error);
                response.setError(error);
                return response;
            }
            username = authTakeSessionResponse.getUsername();

            // Call Auth-3 Confirm OTP
            AuthOTPResponse authOTPResponse = authenClient.confirmOTP(location, AuthRequest.builder()
                    .sessionId((String) requestTransaction.get("sessionId"))
                    .username(username).otp((String) requestTransaction.get("OTP"))
                    .transTime(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()))
                    .transId((String) requestTransaction.get("transactionId"))
                    .build(), header);
            error = authOTPResponse.getError();
            if (error != null) {
                log.error("{}:{}", location + "#After call Auth-3", error);
                response.setError(error);
                return response;
            }

            String customerId = (String) requestTransaction.get("customerID");
            TransactionInfo transactionInfo = TransactionInfo.builder()
                    .transactionId(String.join("_", customerId, new SimpleDateFormat("ddMMyyyy-HHmmss").format(new Date())))
                    .customerId(customerId)
                    .otp((String) requestTransaction.get("OTP"))
                    .approvalMethod("SOFTOTP")
                    .insertTime(new Date())
                    .status(TransactionStatus.PROCESSING.name())
                    .build();
            try {
                transactionInfoDAO.insert(transactionInfo);
            } catch (Exception e) {
                throw new InsertFailedException(e);
            }

            //Call T2405 - Funds transfer logic
            T24FundTransferResponse t24FundTransferResponse = t24UtilClient.fundTransfer(location, T24Request.builder().authenType("KAI.API.FT.PROCESS")
                    .transactionId((String) requestTransaction.get("transactionId"))
                    .debitAccount((String) requestTransaction.get("debitAccount"))
                    .creditAccount((String) requestTransaction.get("creditAccount"))
                    .bankId((String) requestTransaction.get("bankId"))
                    .transAmount((String) requestTransaction.get("transAmount"))
                    .transDesc((String) requestTransaction.get("transDesc"))
                    .build(), header);

            error = t24FundTransferResponse.getError();
            HashMap<String, Object> params = new HashMap();
            if (error != null) {
                params.put("response_code", error.getCode());
                params.put("response_str", error.getDesc());
                params.put("status", TransactionStatus.ERROR);
                log.error("{}:{}", location + "#After call T2405", error);
                try {
                    transactionInfoDAO.update(transactionInfo.getTransactionId(), params);
                } catch (Exception e) {
                    throw new UpdateFailedException(e);
                }
                response.setError(error);
                return response;
            }
            params.put("response_code", t24FundTransferResponse.getResponseCode());
            params.put("bank_trans_id", t24FundTransferResponse.getTransactionNO());
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
                    .transRef(t24FundTransferResponse.getTransactionNO())
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
            responseTransaction.put("responseCode", t24FundTransferResponse.getResponseCode());
            responseTransaction.put("transactionNO", t24FundTransferResponse.getTransactionNO());
            responseTransaction.put("napasRef", ((HashMap) napas2Response.getBody().get("transaction")).get("napasRef"));
            body.put("transaction", responseTransaction);
            response.setBody(body);
            log.info("{}#END", location);
            return response;
        }, request, location);
    }

    private ApiHeader copyHeaders(ApiHeader origHeader) {
        ApiHeader header = new ApiHeader();
        header.setReqType(origHeader.getReqType());
        header.setChannel(origHeader.getChannel());
        header.setContext(origHeader.getContext());
        header.setDuration(origHeader.getDuration());
        header.setLocation(origHeader.getLocation());
        header.setPriority(origHeader.getPriority());
        header.setSynasyn(origHeader.getSynasyn());
        header.setRequestAPI(requestApi);
        header.setRequestNode(ApiUtils.getCurrentHostName());
        return header;
    }
}
