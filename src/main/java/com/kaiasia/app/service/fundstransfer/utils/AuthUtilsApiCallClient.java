package com.kaiasia.app.service.fundstransfer.utils;

import ms.apiclient.model.ApiRequest;
import com.kaiasia.app.service.fundstransfer.configuration.KaiApiRequestBuilderFactory;
import com.kaiasia.app.service.fundstransfer.model.request.Auth1In;
import com.kaiasia.app.service.fundstransfer.model.request.FundsTransferIn;
import com.kaiasia.app.service.fundstransfer.model.response.Auth1Out;
import com.kaiasia.app.service.fundstransfer.model.response.Auth3Out;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@ConfigurationProperties(prefix = "dep-api.auth-api")
@Component
@Data
@Slf4j
public class AuthUtilsApiCallClient extends ApiCallClient {
//    private final KaiApiRequestBuilderFactory kaiApiRequestBuilderFactory;
//
//    public Auth1Out callAuth1(String location, ApiRequest request){
//        FundsTransferIn requestData = ObjectAndJsonUtils.fromObject(request.getBody()
//                                                                           .get("transaction"), FundsTransferIn.class);
//        ApiRequest auth1Request = kaiApiRequestBuilderFactory.getBuilder()
//                                                             .api(this.apiName)
//                                                             .apiKey(this.apiKey)
//                                                             .bodyProperties("command", "GET_ENQUIRY")
//                                                             .bodyProperties("enquiry", new Auth1In(this.authenType.get("auth-1"), requestData.getSessionId()))
//                                                             .build();
//        return this.call(location, auth1Request, Auth1Out.class);
//    }
//
//    public Auth3Out callAuth3(String location, ApiRequest request){
//        return this.call(location,request,Auth3Out.class);
//    }
//    @PostConstruct
//    public void init() {
//        System.out.println(this.url + " " + this.apiName+ " " + this.apiKey + " " + this.authenType);
//    }
}
