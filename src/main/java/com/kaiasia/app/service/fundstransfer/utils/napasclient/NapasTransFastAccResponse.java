package com.kaiasia.app.service.fundstransfer.utils.napasclient;

import lombok.Data;
import ms.apiclient.model.ApiError;

@Data
public class NapasTransFastAccResponse {
    private String napasRef;
    private String responseCode;
    private ApiError error = new ApiError(ApiError.OK_CODE, ApiError.OK_DESC);
}
