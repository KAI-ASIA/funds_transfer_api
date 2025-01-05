package com.kaiasia.app.service.fundstransfer.utils;

import com.kaiasia.app.core.model.ApiRequest;
import com.kaiasia.app.service.fundstransfer.model.response.FundsTransferOut;

public class T24UtilsApiCallClient extends ApiCallClient {
    public FundsTransferOut callFundTransfer(String location,ApiRequest request){
        return this.call(location,request,FundsTransferOut.class);
    }
}
