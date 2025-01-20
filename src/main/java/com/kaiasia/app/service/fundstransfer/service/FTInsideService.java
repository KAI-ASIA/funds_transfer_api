package com.kaiasia.app.service.fundstransfer.service;

import com.kaiasia.app.core.utils.ApiConstant;
import com.kaiasia.app.core.utils.GetErrorUtils;
import com.kaiasia.app.register.KaiMethod;
import com.kaiasia.app.register.KaiService;
import com.kaiasia.app.register.Register;
import com.kaiasia.app.service.fundstransfer.configuration.KaiApiRequestBuilderFactory;
import com.kaiasia.app.service.fundstransfer.dao.ITransactionInfoDAO;
import com.kaiasia.app.service.fundstransfer.exception.ExceptionHandler;
import com.kaiasia.app.service.fundstransfer.exception.InsertFailedException;
import com.kaiasia.app.service.fundstransfer.exception.UpdateFailedException;
import com.kaiasia.app.service.fundstransfer.model.entity.TransactionInfo;
import com.kaiasia.app.service.fundstransfer.model.enums.TransactionStatus;
import com.kaiasia.app.service.fundstransfer.model.request.FundsTransferIn;
import com.kaiasia.app.service.fundstransfer.model.validation.FundsTransferOptional;
import com.kaiasia.app.service.fundstransfer.utils.ObjectAndJsonUtils;
import com.kaiasia.app.service.fundstransfer.utils.ServiceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms.apiclient.authen.*;
import ms.apiclient.model.*;
import ms.apiclient.t24util.T24FundTransferResponse;
import ms.apiclient.t24util.T24Request;
import ms.apiclient.t24util.T24UtilClient;
import org.springframework.scheduling.annotation.Async;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@KaiService
@Slf4j
@RequiredArgsConstructor
public class FTInsideService {
    private final GetErrorUtils apiErrorUtils;
    private final KaiApiRequestBuilderFactory kaiApiRequestBuilderFactory;
    private final ITransactionInfoDAO transactionInfoDAO;
    private final ExceptionHandler exceptionHandler;
    private final AuthenClient authenClient;
    private final T24UtilClient t24UtilClient;
    private final AsyncTask asyncTask;

    @KaiMethod(name = "KAI.API.FT.IN", type = Register.VALIDATE)
    public ApiError validate(ApiRequest req) {
        return ServiceUtils.validate(req, FundsTransferIn.class, apiErrorUtils, "TRANSACTION", FundsTransferOptional.class);
    }

    @KaiMethod(name = "KAI.API.FT.IN")
    public ApiResponse process(ApiRequest req) throws Exception {
        FundsTransferIn requestData = ObjectAndJsonUtils.fromObject(req
                .getBody()
                .get("transaction"), FundsTransferIn.class);
        String location = "FTInside-" + requestData.getSessionId() + "-" + System.currentTimeMillis();

        return exceptionHandler.handle(request -> {
            ApiResponse response = new ApiResponse();
            ApiHeader header = req.getHeader();
            response.setHeader(header);
            ApiError error = new ApiError();
            ApiBody body = new ApiBody();

            // Call Auth-1 api
            AuthTakeSessionResponse auth1Response = null;
            auth1Response = authenClient.takeSession(location,
                    AuthRequest.builder()
                               .sessionId(requestData.getSessionId())
                               .build(),
                    request.getHeader());
            error = auth1Response.getError();
            if (!ApiError.OK_CODE.equals(error.getCode())) {
                log.error("{}:{}", location + "#After call Auth-1", error);
                response.setError(error);
                return response;
            }

            // Call Auth-3 api
            // Chuyển đổi sang định dạng yyyyMMddHHmmss
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//
//            AuthOTPResponse auth3Response = null;
//            auth3Response = authenClient.confirmOTP(location,
//                    AuthRequest.builder()
//                               .username(auth1Response.getUsername())
//                               .sessionId(auth1Response.getSessionId())
//                               .otp(requestData.getOtp())
//                               .transTime(sdf.format(new Date()))
//                               .transId(requestData.getTransactionId())
//                               .build(),
//                    request.getHeader());
//            error = auth3Response.getError();
//            if (!ApiError.OK_CODE.equals(error.getCode())) {
//                log.error("{}:{}", location + "#After call Auth-3", error);
//                response.setError(error);
//                return response;
//            }

            TransactionInfo transactionInfo = TransactionInfo.builder()
                                                             .transactionId(requestData.getTransactionId())
                                                             .customerId(requestData.getCustomerID())
                                                             .debitAccount(requestData.getDebitAccount())
                                                             .creditAccount(requestData.getCreditAccount())
                                                             .otp(requestData.getOtp())
                                                             .approvalMethod("SOFTOTP")
                                                             .amount(requestData.getTransAmount())
                                                             .bankCode("300")
                                                             .insertTime(new Date())
                                                             .status(ApiConstant.STATUS.PROCESSING)
                                                             .build();
            // Insert vào db
            try {
                transactionInfoDAO.insert(transactionInfo);
                log.warn("insert OK");
            } catch (Exception e) {
                log.error("insert failed", e);
                throw new InsertFailedException(e);
            }
            HashMap<String, Object> params = new HashMap<>();

            // Call T2405 api
            T24FundTransferResponse t2405Response = t24UtilClient.fundTransfer(location,
                    T24Request.builder()
                              .transactionId(requestData.getTransactionId())
                              .debitAccount(requestData.getDebitAccount())
                              .creditAccount(requestData.getCreditAccount())
                              .bankId("300")
                              .transAmount(requestData.getTransAmount())
                              .transDesc(requestData.getTransDesc())
                              .company(requestData.getCompany())
                              .channel("EBANK")
                              .build(),
                    request.getHeader());

            log.warn("#{}{}", t2405Response.getTransactionNO(), t2405Response.getResponseCode());

            error = t2405Response.getError();
            if (!ApiError.OK_CODE.equals(error.getCode())) {
                // Trường hợp timeout
                if("998".equals(error.getCode())) {
                    log.error("#{}:{}", location + "#After call T2405", error);
                    params.put("response_code", t2405Response.getError().getCode());
                    params.put("response_str", t2405Response.getError().getDesc());
                    params.put("status", TransactionStatus.CONSOLIDATION.toString());
                    try {
                        transactionInfoDAO.update(transactionInfo.getTransactionId(), params);
                    } catch (Exception e) {
                        throw new UpdateFailedException(e);
                    }
                    response.setError(error);
                    return response;
                }
                // Trường hợp lỗi khác
                else {
                    log.error("#{}:{}", location + "#After call T2405", error);
                    params.put("status", TransactionStatus.ERROR.toString());
                    try {
                        transactionInfoDAO.update(transactionInfo.getTransactionId(), params);
                    } catch (Exception e) {
                        throw new UpdateFailedException(e);
                    }
                    response.setError(error);
                    return response;
                }
            }

            params.put("response_code", t2405Response.getResponseCode());
            params.put("bank_trans_id", t2405Response.getTransactionNO());
            params.put("last_update", new Date());
            params.put("status", TransactionStatus.DONE.toString());

            // Cập nhật thông tin vào db Transaction_info
            asyncTask.asyncUpdateTransaction(transactionInfo.getTransactionId(), params);

            header.setReqType("RESPONSE");
            body.put("transaction", t2405Response);
            response.setBody(body);
            log.warn("##End process method.");
            return response;
        }, req, "#FundsTransferOutSide/" + requestData.getSessionId() + "/" + System.currentTimeMillis());
    }
}
