package com.kaiasia.app.service.fundstransfer.service;

import com.kaiasia.app.core.model.*;
import com.kaiasia.app.core.utils.GetErrorUtils;
import com.kaiasia.app.register.KaiMethod;
import com.kaiasia.app.register.KaiService;
import com.kaiasia.app.register.Register;
import com.kaiasia.app.service.fundstransfer.configuration.DepApiConfig;
import com.kaiasia.app.service.fundstransfer.configuration.DepApiProperties;
import com.kaiasia.app.service.fundstransfer.configuration.KaiApiRequestBuilderFactory;
import com.kaiasia.app.service.fundstransfer.model.*;
import com.kaiasia.app.service.fundstransfer.utils.ApiCallHelper;
import com.kaiasia.app.service.fundstransfer.utils.ObjectAndJsonUtils;
import com.kaiasia.app.service.fundstransfer.utils.ServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

@KaiService
@Slf4j
public class FTInsideService {

    @Autowired
    GetErrorUtils apiErrorUtils;

    @Autowired
    private DepApiConfig depApiConfig;

    @Autowired
    private KaiApiRequestBuilderFactory kaiApiRequestBuilderFactory;

    @KaiMethod(name = "FTInsideService", type = Register.VALIDATE)
    public ApiError validate(ApiRequest req) {
        return ServiceUtils.validate(req, FundsTransferIn.class, apiErrorUtils, "TRANSACTION");
    }

    @KaiMethod(name = "FTInsideService")
    public ApiResponse process(ApiRequest req) {
        ApiResponse response = new ApiResponse();
        ApiHeader header = req.getHeader();
        header.setReqType("RESPONSE");
        response.setHeader(header);
        ApiError error = null;
        ApiBody body = new ApiBody();

        DepApiProperties authApiProperties = depApiConfig.getAuthApi();
        FundsTransferIn requestData = ObjectAndJsonUtils.fromObject(req.getBody().get("transaction"), FundsTransferIn.class);
        String location = "FTInside-" + requestData.getSessionId() + "-" + requestData.getCustomerID();

        // Call Auth-1 api
        Auth1In auth1In = new Auth1In("takeSession", requestData.getSessionId());
        ApiRequest auth1Request = kaiApiRequestBuilderFactory.getBuilder()
                                                             .api(authApiProperties.getApiName())
                                                             .apiKey(authApiProperties.getApiKey())
                                                             .bodyProperties("command", "GET_ENQUIRY")
                                                             .bodyProperties("enquiry", auth1In)
                                                             .build();

        String username = null;

        try {
            ApiResponse auth1Response = ApiCallHelper.call(authApiProperties.getUrl(), HttpMethod.POST, ObjectAndJsonUtils.toJson(auth1Request), ApiResponse.class);
            error = auth1Response.getError();
            if (error != null || !"OK".equals(auth1Response.getBody().get("status"))) {
                log.error("{}:{}", location + "#After call Auth-1", error);
                response.setError(error);
                return response;
            }

            // Kiểm tra kết quả trả về đủ field không.
            ApiError validateError = ServiceUtils.validate(auth1Response, Auth1Out.class, apiErrorUtils, "TRANSACTION");
            if (!validateError.getCode().equals(ApiError.OK_CODE)) {
                log.error("{}:{}", location + "#After call Auth-1", validateError);
                response.setError(validateError);
                return response;
            }
            Auth1Out auth1ResponseData = ObjectAndJsonUtils.fromObject(auth1Response.getBody().get("enquiry"), Auth1Out.class);
            username = auth1ResponseData.getUsername();
        } catch (Exception e) {
            log.error("{}:{}", location + "#Calling Auth-1", e.getMessage());
            error = apiErrorUtils.getError("999", new String[]{e.getMessage()});
            response.setError(error);
            return response;
        }

        // Call Auth-3 api
        long currentTimeMillis = System.currentTimeMillis();

        // Chuyển đổi sang định dạng yyyyMMddHHmmss
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String formattedDate = sdf.format(new Date(currentTimeMillis));

        Auth3In auth3In = new Auth3In("confirmOTP", requestData.getSessionId(), username, requestData.getOtp(), formattedDate, requestData.getTransactionId());
        ApiRequest auth3Request = kaiApiRequestBuilderFactory.getBuilder()
                                                             .api(authApiProperties.getApiName())
                                                             .apiKey(authApiProperties.getApiKey())
                                                             .bodyProperties("command", "GET_ENQUIRY")
                                                             .bodyProperties("enquiry", auth3In)
                                                             .build();

        try {
            ApiResponse auth3Response = ApiCallHelper.call(authApiProperties.getUrl(), HttpMethod.POST, ObjectAndJsonUtils.toJson(auth3Request), ApiResponse.class);
            error = auth3Response.getError();
            if (error != null || !"OK".equals(auth3Response.getBody().get("status"))) {
                log.error("{}:{}", location + "#After call Auth-3", error);
                response.setError(error);
                return response;
            }

            // Kiểm tra kết quả trả về đủ field không.
            ApiError validateError = ServiceUtils.validate(auth3Response, Auth3Out.class, apiErrorUtils, "TRANSACTION");
            if (!validateError.getCode().equals(ApiError.OK_CODE)) {
                log.error("{}:{}", location + "#After call Auth-3", validateError);
                response.setError(validateError);
                return response;
            }
        } catch (Exception e) {
            log.error("{}:{}", location + "#Calling Auth-3", e.getMessage());
            error = apiErrorUtils.getError("999", new String[]{e.getMessage()});
            response.setError(error);
            return response;
        }

        //TODO : Lưu thông tin vào db Transaction_info

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

        try {
            ApiResponse t24Response = ApiCallHelper.call(t24ApiProperties.getUrl(), HttpMethod.POST, ObjectAndJsonUtils.toJson(t2405Request), ApiResponse.class);
            error = t24Response.getError();
            if (error != null || !"OK".equals(t24Response.getBody().get("status"))) {
                log.error("{}:{}", location + "#After call T2405", error);
                response.setError(error);
                return response;
            }

            // Kiểm tra kết quả trả về đủ field không.
            ApiError validateError = ServiceUtils.validate(t24Response, FundsTransferOut.class, apiErrorUtils, "TRANSACTION");
            if (!validateError.getCode().equals(ApiError.OK_CODE)) {
                log.error("{}:{}", location + "#After call T2405", validateError);
                response.setError(validateError);
                return response;
            }
            fundsTransferOut = ObjectAndJsonUtils.fromObject(t24Response.getBody().get("transaction"), FundsTransferOut.class);
        } catch (Exception e) {
            log.error("{}:{}", location + "#Calling T2405", e.getMessage());
            error = apiErrorUtils.getError("999", new String[]{e.getMessage()});
            response.setError(error);
            return response;
        }

        // TODO : Cập nhật thông tin vào db Transaction_info

        body.put("transaction", fundsTransferOut);
        response.setBody(body);
        return response;
    }
}
