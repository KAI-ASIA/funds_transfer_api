package com.kaiasia.app.service.fundstransfer.utils.napasclient;

import lombok.Data;
import ms.apiclient.model.ApiError;

@Data
public class NapasGetAccountResponse {
    private AccountInfo accountInfo;
    private String responseCode;
    private ApiError error = new ApiError(ApiError.OK_CODE,ApiError.OK_DESC);

    @Data
    static class AccountInfo{
        private String  bankId;
        private String accountId;
        private String accountName;
    }
}
