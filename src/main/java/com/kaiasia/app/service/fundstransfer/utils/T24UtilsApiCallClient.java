package com.kaiasia.app.service.fundstransfer.utils;

import com.kaiasia.app.core.model.ApiRequest;
import com.kaiasia.app.service.fundstransfer.model.reponse.FundsTransferOut;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Setter
@Component
@ConfigurationProperties(prefix = "dep-api.t24utils-api")
public class T24UtilsApiCallClient extends ApiCallClient {
    public FundsTransferOut callFundTransfer(String location,ApiRequest request){
        return this.call(location,request,FundsTransferOut.class);
    }
}
