package com.kaiasia.app.service.fundstransfer.service;

import com.kaiasia.app.core.model.*;
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
import com.kaiasia.app.service.fundstransfer.model.request.Auth1In;
import com.kaiasia.app.service.fundstransfer.model.request.Auth3In;
import com.kaiasia.app.service.fundstransfer.model.request.FundsTransferIn;
import com.kaiasia.app.service.fundstransfer.model.response.Auth1Out;
import com.kaiasia.app.service.fundstransfer.model.response.Auth3Out;
import com.kaiasia.app.service.fundstransfer.model.response.FundsTransferOut;
import com.kaiasia.app.service.fundstransfer.utils.ApiCallHelper;
import com.kaiasia.app.service.fundstransfer.utils.ObjectAndJsonUtils;
import com.kaiasia.app.service.fundstransfer.utils.ServiceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

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

    @KaiMethod(name = "FTInsideService", type = Register.VALIDATE)
    public ApiError validate(ApiRequest req) {
        return ServiceUtils.validate(req, FundsTransferIn.class, apiErrorUtils, "TRANSACTION");
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
            header.setReqType("RESPONSE");
            response.setHeader(header);
            ApiError error = null;
            ApiBody body = new ApiBody();

            DepApiProperties authApiProperties = depApiConfig.getAuthApi();

            // Call Auth-1 api
            Auth1In auth1In = new Auth1In("takeSession", requestData.getSessionId());
            ApiRequest auth1Request = kaiApiRequestBuilderFactory.getBuilder()
                                                                 .api(authApiProperties.getApiName())
                                                                 .apiKey(authApiProperties.getApiKey())
                                                                 .bodyProperties("command", "GET_ENQUIRY")
                                                                 .bodyProperties("enquiry", auth1In)
                                                                 .build();

            String username = null;

            ApiResponse auth1Response = ApiCallHelper.call(authApiProperties.getUrl(),
                    HttpMethod.POST,
                    ObjectAndJsonUtils.toJson(auth1Request),
                    ApiResponse.class,
                    authApiProperties.getTimeout());
            error = auth1Response.getError();
            if (error != null || !"OK".equals(auth1Response.getBody().get("status"))) {
                log.error("{}:{}", location + "#After call Auth-1", error);
                response.setError(error);
                return response;
            }

            // Kiểm tra kết quả trả về đủ field không.
            ApiError validateAuth1Error = ServiceUtils.validate(auth1Response, Auth1Out.class, apiErrorUtils, "TRANSACTION");
            if (!validateAuth1Error.getCode().equals(ApiError.OK_CODE)) {
                log.error("{}:{}", location + "#After call Auth-1", validateAuth1Error);
                response.setError(validateAuth1Error);
                return response;
            }
            Auth1Out auth1ResponseData = ObjectAndJsonUtils.fromObject(auth1Response
                    .getBody()
                    .get("enquiry"), Auth1Out.class);
            username = auth1ResponseData.getUsername();

            // Call Auth-3 api
            // Chuyển đổi sang định dạng yyyyMMddHHmmss
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

            Auth3In auth3In = new Auth3In("confirmOTP", requestData.getSessionId(), username, requestData.getOtp(), sdf.format(new Date()), requestData.getTransactionId());
            ApiRequest auth3Request = kaiApiRequestBuilderFactory.getBuilder()
                                                                 .api(authApiProperties.getApiName())
                                                                 .apiKey(authApiProperties.getApiKey())
                                                                 .bodyProperties("command", "GET_ENQUIRY")
                                                                 .bodyProperties("enquiry", auth3In)
                                                                 .build();

            ApiResponse auth3Response = ApiCallHelper.call(authApiProperties.getUrl(), HttpMethod.POST, ObjectAndJsonUtils.toJson(auth3Request), ApiResponse.class, authApiProperties.getTimeout());
            error = auth3Response.getError();
            if (error != null || !"OK".equals(auth3Response.getBody().get("status"))) {
                log.error("{}:{}", location + "#After call Auth-3", error);
                response.setError(error);
                return response;
            }

            // Kiểm tra kết quả trả về từ Auth-3 đủ field không.
            ApiError validateAuth3Error = ServiceUtils.validate(auth3Response, Auth3Out.class, apiErrorUtils, "TRANSACTION");
            if (!validateAuth3Error.getCode().equals(ApiError.OK_CODE)) {
                log.error("{}:{}", location + "#After call Auth-3", validateAuth3Error);
                response.setError(validateAuth3Error);
                return response;
            }
            TransactionInfo transactionInfo = TransactionInfo.builder()
                                                             .transactionId(requestData.getCustomerID() + "-" + new SimpleDateFormat("ddMMyyyy").format(new Date()))
                                                             .customerId(requestData.getCustomerID())
                                                             .otp(requestData.getOtp())
                                                             .approvalMethod("SOFTOTP")
                                                             .insertTime(new Date())
                                                             .status(ApiConstant.STATUS.PROCESSING)
                                                             .build();
            // Insert vào db
            try {
                transactionInfoDAO.insert(transactionInfo);
            } catch (Exception e) {
                throw new InsertFailedException(e);
            }

            // Call T2405 api
            DepApiProperties t24ApiProperties = depApiConfig.getT24utilsApi();
            FundsTransferIn fundsTransferIn = FundsTransferIn.builder()
                                                             .authenType("fundTransfer")
                                                             .transactionId(requestData.getTransactionId())
                                                             .debitAccount(requestData.getDebitAccount())
                                                             .creditAccount(requestData.getCreditAccount())
                                                             .transAmount(requestData.getTransAmount())
                                                             .transDesc(requestData.getTransDesc())
                                                             .build();
            ApiRequest t2405Request = kaiApiRequestBuilderFactory.getBuilder()
                                                                 .api(t24ApiProperties.getApiName())
                                                                 .apiKey(t24ApiProperties.getApiKey())
                                                                 .bodyProperties("command", "GET_TRANSACTION")
                                                                 .bodyProperties("enquiry", fundsTransferIn)
                                                                 .build();
            FundsTransferOut fundsTransferOut = null;

            HashMap<String, Object> params = new HashMap<>();

            ApiResponse t24Response = ApiCallHelper.call(t24ApiProperties.getUrl(), HttpMethod.POST, ObjectAndJsonUtils.toJson(t2405Request), ApiResponse.class, t24ApiProperties.getTimeout());
            error = t24Response.getError();
            if (error != null || !"OK".equals(t24Response.getBody().get("status"))) {
                log.error("{}:{}", location + "#After call T2405", error);
                params.put("status", ApiConstant.STATUS.ERROR);
                try {
                    transactionInfoDAO.update(transactionInfo.getTransactionId(), params);
                } catch (Exception e) {
                    throw new UpdateFailedException(e);
                }
                response.setError(error);
                return response;
            }

            // Kiểm tra kết quả trả về đủ field không.
            ApiError validateT2405Error = ServiceUtils.validate(t24Response, FundsTransferOut.class, apiErrorUtils, "TRANSACTION");
            if (!validateT2405Error.getCode().equals(ApiError.OK_CODE)) {
                log.error("{}:{}", location + "#After call T2405", validateT2405Error);
                params.put("status", ApiConstant.STATUS.ERROR);
                try {
                    transactionInfoDAO.update(transactionInfo.getTransactionId(), params);
                } catch (Exception e) {
                    throw new UpdateFailedException(e);
                }
                response.setError(validateT2405Error);
                return response;
            }
            fundsTransferOut = ObjectAndJsonUtils.fromObject(t24Response.getBody().get("transaction"), FundsTransferOut.class);
            params.put("response_code", fundsTransferOut.getResponseCode());
            params.put("bank_trans_id", fundsTransferOut.getTransactionNO());
            params.put("last_update", new Date());
            params.put("status", ApiConstant.STATUS.DONE);

            // Cập nhật thông tin vào db Transaction_info
            try {
                transactionInfoDAO.update(transactionInfo.getTransactionId(), params);
            } catch (Exception e) {
                throw new UpdateFailedException(e);
            }

            body.put("transaction", fundsTransferOut);
            response.setBody(body);
            return response;
        }, req, "FundsTransferOutSide/" + requestData.getSessionId() + "/" + System.currentTimeMillis());
    }
}
