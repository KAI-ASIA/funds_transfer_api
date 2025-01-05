package com.kaiasia.app.service.fundstransfer.model;


import com.kaiasia.app.utils.ApiUtils;
import ms.apiclient.model.ApiBody;
import ms.apiclient.model.ApiHeader;
import ms.apiclient.model.ApiRequest;


public class KaiApiRequestBuilder {
    private final ApiRequest apiRequest;
    private ApiHeader apiHeader;
    private ApiBody apiBody;

    private KaiApiRequestBuilder() {
        apiRequest = new ApiRequest();
        apiHeader = new ApiHeader();
        apiHeader.setReqType("REQUEST");
        apiHeader.setChannel("API");
        apiHeader.setPriority(1);
        apiHeader.setRequestNode(ApiUtils.getCurrentHostName());
        apiHeader.setLocation("PC");
        apiBody = new ApiBody();
    }

    public static KaiApiRequestBuilder builder() {
        return new KaiApiRequestBuilder();
    }

    public KaiApiRequestBuilder header(ApiHeader apiHeader) {
        this.apiHeader = apiHeader;
        return this;
    }

    public KaiApiRequestBuilder requestBody(ApiBody apiBody) {
        this.apiBody = apiBody;
        return this;
    }

    public KaiApiRequestBuilder reqType(String reqType) {
        this.apiHeader.setReqType(reqType);
        return this;
    }

    public KaiApiRequestBuilder api(String api) {
        this.apiHeader.setApi(api);
        return this;
    }

    public KaiApiRequestBuilder apiKey(String apiKey) {
        this.apiHeader.setApiKey(apiKey);
        return this;
    }

    public KaiApiRequestBuilder channel(String channel) {
        this.apiHeader.setChannel(channel);
        return this;
    }

    public KaiApiRequestBuilder requestApi(String requestApi) {
        this.apiHeader.setRequestAPI(requestApi);
        return this;
    }

    public KaiApiRequestBuilder location(String location) {
        this.apiHeader.setLocation(location);
        return this;
    }

    public KaiApiRequestBuilder duration(long duration) {
        this.apiHeader.setDuration(duration);
        return this;
    }

    public KaiApiRequestBuilder requestNode(String requestNode) {
        this.apiHeader.setRequestNode(requestNode);
        return this;
    }

    public KaiApiRequestBuilder priority(int priority) {
        this.apiHeader.setPriority(priority);
        return this;
    }

    public KaiApiRequestBuilder bodyProperties(String key, Object value) {
        apiBody.put(key, value);
        return this;
    }

    public ApiRequest build() {
        apiRequest.setHeader(apiHeader);
        apiRequest.setBody(apiBody);
        return apiRequest;
    }
}
