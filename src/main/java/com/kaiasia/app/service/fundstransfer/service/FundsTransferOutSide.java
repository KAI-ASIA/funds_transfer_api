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
import com.kaiasia.app.service.fundstransfer.model.enums.TransactionStatus;
import com.kaiasia.app.service.fundstransfer.model.request.FundsTransferIn;
import com.kaiasia.app.service.fundstransfer.model.request.Napas2In;
import com.kaiasia.app.service.fundstransfer.model.validation.FundsTransferOptional;
import com.kaiasia.app.service.fundstransfer.utils.ApiCallHelper;
import com.kaiasia.app.service.fundstransfer.utils.ObjectAndJsonUtils;
import com.kaiasia.app.service.fundstransfer.utils.ServiceUtils;
import com.kaiasia.app.utils.ApiUtils;
import lombok.extern.slf4j.Slf4j;
import ms.apiclient.authen.AuthOTPResponse;
import ms.apiclient.authen.AuthRequest;
import ms.apiclient.authen.AuthTakeSessionResponse;
import ms.apiclient.authen.AuthenClient;
import ms.apiclient.model.*;
import ms.apiclient.t24util.*;
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

    @KaiMethod(name = "KAI.API.FT.OUT", type = Register.VALIDATE)
    public ApiError validate(ApiRequest request) throws Exception {
        return ServiceUtils.validate(request, FundsTransferIn.class, getErrorUtils, "TRANSACTION", FundsTransferOptional.class);
    }

    @KaiMethod(name = "KAI.API.FT.OUT")
    public ApiResponse process(ApiRequest request) {
        FundsTransferIn requestTransaction = ObjectAndJsonUtils.fromObject(request.getBody().get("transaction"), FundsTransferIn.class);
        String location = "FundsTransferOutSide_" + requestTransaction.getSessionId() + "_" + System.currentTimeMillis();
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
                    .sessionId(requestTransaction.getSessionId()).build(), header);
            error = authTakeSessionResponse.getError();
            if (!ApiError.OK_CODE.equals(error.getCode())) {
                log.error("{}#{}", location + "#After call Auth-1", error);
                response.setError(error);
                return response;
            }
            username = authTakeSessionResponse.getUsername();

            // Call Auth-3 Confirm OTP
            AuthOTPResponse authOTPResponse = authenClient.confirmOTP(location, AuthRequest.builder()
                    .sessionId(requestTransaction.getSessionId())
                    .username(username).otp(requestTransaction.getOtp())
                    .transTime(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()))
                    .transId(requestTransaction.getTransactionId())
                    .build(), header);
            error = authOTPResponse.getError();
            if (!ApiError.OK_CODE.equals(error.getCode())) {
                log.error("{}:{}", location + "#After call Auth-3", error);
                response.setError(error);
                return response;
            }

            String customerId = requestTransaction.getCustomerID();
            TransactionInfo transactionInfo = TransactionInfo.builder()
                    .transactionId(requestTransaction.getTransactionId())
                    .customerId(customerId)
                    .otp(requestTransaction.getOtp())
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
                    .transactionId(requestTransaction.getTransactionId())
                    .debitAccount(requestTransaction.getDebitAccount())
                    .creditAccount(requestTransaction.getCreditAccount())
                    .bankId(requestTransaction.getBankId())
                    .transAmount(requestTransaction.getTransAmount())
                    .transDesc(requestTransaction.getTransDesc())
                    .build(), header);

            error = t24FundTransferResponse.getError();
            HashMap<String, Object> params = new HashMap();
            if (!ApiError.OK_CODE.equals(error.getCode())) {
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
            try {
                transactionInfoDAO.update(transactionInfo.getTransactionId(), params);
            } catch (Exception e) {
                throw new UpdateFailedException(e);
            }

            //Call NAPAS-2
            DepApiProperties napasApiProperties = depApiConfig.getNapasApi();
            Napas2In napas2RequestTransaction = Napas2In.builder().authenType("getTransFastAcc")
                    .senderAccount(requestTransaction.getDebitAccount())
                    .amount(requestTransaction.getTransAmount()).ccy("VND")
                    .transRef(t24FundTransferResponse.getTransactionNO())
                    .benAcc(requestTransaction.getCreditAccount())// chua ro rang du lieu
                    .bankId(requestTransaction.getBankId())
                    .transContent(requestTransaction.getTransDesc())
                    .build();
            ApiRequest napas2Request = kaiApiRequestBuilderFactory.getBuilder()
                    .api(napasApiProperties.getApiName())
                    .apiKey(napasApiProperties.getApiKey())
                    .bodyProperties("command", "GET_TRANSACTION")
                    .bodyProperties("enquiry", napas2RequestTransaction)
                    .build();

            ApiResponse napas2Response = ApiCallHelper.call(napasApiProperties.getUrl(), HttpMethod.POST, ObjectAndJsonUtils.toJson(napas2Request), ApiResponse.class, napasApiProperties.getTimeout());
            error = napas2Response.getError();
            if (!ApiError.OK_CODE.equals(error.getCode())) {
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
