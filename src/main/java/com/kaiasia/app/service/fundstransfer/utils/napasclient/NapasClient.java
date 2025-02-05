package com.kaiasia.app.service.fundstransfer.utils.napasclient;

import ms.apiclient.client.common.ApiCallClient;
import ms.apiclient.model.ApiHeader;
import ms.apiclient.model.ApiRequest;

public class NapasClient extends ApiCallClient {

    public NapasGetAccountResponse getAccountNapas(String location, NapasRequest request, ApiHeader header) {
        request.setAuthenType("enqCheckAcc");
        ApiRequest apiRequest = buildENQUIRY(request, header);
        return call(location, apiRequest, NapasGetAccountResponse.class);
    }

    public NapasTransFastAccResponse napasTransFastAcc(String location, NapasRequest request, ApiHeader header) {
        request.setAuthenType("getTransFastAcc");
        ApiRequest apiRequest = buildTransaction(request, header);
        return call(location, apiRequest, NapasTransFastAccResponse.class);
    }

}
