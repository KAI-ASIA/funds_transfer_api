package com.kaiasia.app.service.fundstransfer.service;

import com.kaiasia.app.core.utils.ApiConstant;
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
import com.kaiasia.app.service.fundstransfer.model.*;
import com.kaiasia.app.service.fundstransfer.model.enums.TransactionStatus;
import com.kaiasia.app.service.fundstransfer.model.response.Auth1Out;
import com.kaiasia.app.service.fundstransfer.model.response.Auth3Out;
import com.kaiasia.app.service.fundstransfer.model.response.BaseResponse;
import com.kaiasia.app.service.fundstransfer.model.response.FundsTransferOut;
import com.kaiasia.app.service.fundstransfer.model.request.Auth1In;
import com.kaiasia.app.service.fundstransfer.model.request.Auth3In;
import com.kaiasia.app.service.fundstransfer.model.request.FundsTransferIn;
import com.kaiasia.app.service.fundstransfer.model.validation.FundsTransferOptional;
import com.kaiasia.app.service.fundstransfer.model.validation.SuccessGroup;
import com.kaiasia.app.service.fundstransfer.utils.ApiCallHelper;
import com.kaiasia.app.service.fundstransfer.utils.ObjectAndJsonUtils;
import com.kaiasia.app.service.fundstransfer.utils.ServiceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms.apiclient.authen.*;
import ms.apiclient.model.*;
import ms.apiclient.t24util.T24FundTransferResponse;
import ms.apiclient.t24util.T24Request;
import ms.apiclient.t24util.T24UtilClient;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;

import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

@KaiService
@Slf4j
@RequiredArgsConstructor
public class FTInsideService {
    private final GetErrorUtils apiErrorUtils;
    private final DepApiConfig depApiConfig;
    private final KaiApiRequestBuilderFactory kaiApiRequestBuilderFactory;
    private final ITransactionInfoDAO transactionInfoDAO;
    private final ExceptionHandler exceptionHandler;
    private final AuthenClient authenClient;
    private final T24UtilClient t24UtilClient;

    @KaiMethod(name = "FTInsideService", type = Register.VALIDATE)
    public ApiError validate(ApiRequest req) {
        return ServiceUtils.validate(req, FundsTransferIn.class, apiErrorUtils, "TRANSACTION", FundsTransferOptional.class);
    }

    @KaiMethod(name = "FTInsideService")
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


//            // Call Auth-1 api
//            AuthTakeSessionResponse auth1Response = null;
//            try {
//                auth1Response = authenClient.takeSession(location,
//                        AuthRequest.builder()
//                                   .sessionId(requestData.getSessionId())
//                                   .build(),
//                        request.getHeader());
//            } catch (Exception e) {
//                throw new RestClientException(location, e);
//            }
//            error = auth1Response.getError();
//            if (error != null) {
//                log.error("{}:{}", location + "#After call Auth-1", error);
//                response.setError(error);
//                return response;
//            }
//
//            // Kiểm tra kết quả trả về đủ field không.
//            BaseResponse validateAuth1Error = ServiceUtils.validate(ObjectAndJsonUtils.fromObject(auth1Response, Auth1Out.class), SuccessGroup.class);
//            if (!validateAuth1Error.getCode().equals(ApiError.OK_CODE)) {
//                log.error("{}:{}", location + "#After call Auth-1", validateAuth1Error);
//                response.setError(new ApiError(validateAuth1Error.getCode(), validateAuth1Error.getDesc()));
//                return response;
//            }
//
//            // Call Auth-3 api
//            // Chuyển đổi sang định dạng yyyyMMddHHmmss
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//
//            AuthOTPResponse auth3Response = null;
//            try {
//                auth3Response = authenClient.confirmOTP(location,
//                        AuthRequest.builder()
//                                   .username(auth1Response.getUsername())
//                                   .sessionId(auth1Response.getSessionId())
//                                   .otp(requestData.getOtp())
//                                   .transTime(sdf.format(new Date()))
//                                   .transId(requestData.getTransactionId())
//                                   .build(),
//                        request.getHeader());
//            } catch (Exception e) {
//                throw new RestClientException(location, e);
//            }
//            error = auth3Response.getError();
//            if (error != null) {
//                log.error("{}:{}", location + "#After call Auth-3", error);
//                response.setError(error);
//                return response;
//            }
//
//            // Kiểm tra kết quả trả về đủ field không.
//            BaseResponse validateAuth3Error = ServiceUtils.validate(ObjectAndJsonUtils.fromObject(auth3Response, Auth3Out.class), SuccessGroup.class);
//            if (!validateAuth3Error.getCode().equals(ApiError.OK_CODE)) {
//                log.error("{}:{}", location + "#After call Auth-3", validateAuth3Error);
//                response.setError(new ApiError(validateAuth3Error.getCode(), validateAuth3Error.getDesc()));
//                return response;
//            }
//
            TransactionInfo transactionInfo = TransactionInfo.builder()
                                                             .transactionId(requestData.getTransactionId())
                                                             .customerId(requestData.getCustomerID())
                                                             .otp(requestData.getOtp())
                                                             .approvalMethod("SOFTOTP")
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
            if (error != null) {
                // Trường hợp timeout
                if("998".equals(error.getCode())) {
                    log.error("#{}:{}", location + "#After call T2405", error);
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

            // Kiểm tra kết quả trả về đủ field không.
            BaseResponse validateT2505Error = ServiceUtils.validate(ObjectAndJsonUtils.fromObject(t2405Response, FundsTransferOut.class), SuccessGroup.class);
            if (!validateT2505Error.getCode().equals(ApiError.OK_CODE)) {
                log.error("#{}:{}", location + "#After call T2405", validateT2505Error);
                params.put("status", TransactionStatus.ERROR.toString());
                try {
                    transactionInfoDAO.update(transactionInfo.getTransactionId(), params);
                } catch (Exception e) {
                    throw new UpdateFailedException(e);
                }
                response.setError(new ApiError(validateT2505Error.getCode(), validateT2505Error.getDesc()));
                return response;
            }

            params.put("response_code", t2405Response.getResponseCode());
            params.put("bank_trans_id", t2405Response.getTransactionNO());
            params.put("last_update", new Date());
            params.put("status", TransactionStatus.DONE.toString());

            // Cập nhật thông tin vào db Transaction_info
            try {
                transactionInfoDAO.update(transactionInfo.getTransactionId(), params);
            } catch (Exception e) {
                throw new UpdateFailedException(e);
            }

            header.setReqType("RESPONSE");
            body.put("transaction", t2405Response);
            response.setBody(body);
            return response;
        }, req, "#FundsTransferOutSide/" + requestData.getSessionId() + "/" + System.currentTimeMillis());
    }
}
