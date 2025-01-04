package ms.apiclient.client.common;

import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ms.apiclient.model.ApiBody;
import ms.apiclient.model.ApiError;
import ms.apiclient.model.ApiHeader;
import ms.apiclient.model.ApiRequest;
import ms.apiclient.model.ApiResponse;

@Component
public abstract class CallApiHelper {
    @Autowired
    private ApiRestTemplate apiRestTemplate;

    private String url;
    private String apiName;
    private String apiKey;
    private int apiTimeout;

    public Map<String, Object> getTransaction(ApiResponse response) {
        return (Map<String, Object>) response.getBody().get("transaction");
    }

    public static Map<String, Object> getEnquiry(ApiResponse response) {
        return (Map<String, Object>) response.getBody().get("enquiry");
    }

    public <T> T commonRest(String location, ApiRequest apiReq, Class<T> classResult) {
        ApiError apiError = new ApiError();
        long a = System.currentTimeMillis();
        try {
            ApiHeader headerApi = this.rebuildApiHeader(apiReq.getHeader());
            ApiRequest apiReqRebuild = new ApiRequest();
            apiReqRebuild.setHeader(headerApi);
            apiReqRebuild.setBody(apiReq.getBody());
            ApiResponse response = apiRestTemplate.callApi(apiReqRebuild, url, apiTimeout);
            if (response != null && response.getError() != null) {
                apiError = response.getError();
                ModelMapper mapper = new ModelMapper();
                return mapper.map(apiError, classResult);
            }
            Map<String, Object> enquiryMap = getEnquiry(response);
            if (enquiryMap == null) {
                enquiryMap = getTransaction(response);
            }
            ModelMapper mapper = new ModelMapper();

            return mapper.map(enquiryMap, classResult);
        } catch (Exception eis) {
            apiError.setCode("TIMEOUT");
            apiError.setDesc(eis.toString());
            ModelMapper mapper = new ModelMapper();
            return mapper.map(apiError, classResult);
        }
    }


    private ApiHeader rebuildApiHeader(ApiHeader apiHeader) {
        ApiHeader headerApi = new ApiHeader();
        headerApi.setChannel(apiHeader.getChannel());
        headerApi.setContext(apiHeader.getContext());
        headerApi.setLocation(apiHeader.getLocation());
        headerApi.setRequestNode(apiHeader.getRequestNode());
        headerApi.setReqType("REQUEST");
        headerApi.setRequestAPI(apiHeader.getRequestAPI());
        headerApi.setSynasyn("true");
        headerApi.setApi(apiName);
        headerApi.setApiKey(apiKey);
        return headerApi;
    }

    public static <T> ApiRequest buildENQUIRY(T enquiryInput, ApiHeader header) {
        ApiRequest apiReq = new ApiRequest();
        apiReq.setHeader(header);
        ApiBody apiBody = new ApiBody();
        apiBody.put("COMMAND", "GET_ENQUIRY");
        apiBody.put("ENQUIRY", enquiryInput);
        apiReq.setBody(apiBody);
        return apiReq;
    }


    public static <T> ApiRequest buildTransaction(T transactionInput, ApiHeader header) {
        ApiRequest apiReq = new ApiRequest();
        apiReq.setHeader(header);
        ApiBody apiBody = new ApiBody();
        apiBody.put("COMMAND", "GET_TRANSACTION");
        apiBody.put("TRANSACTION", transactionInput);
        apiReq.setBody(apiBody);
        return apiReq;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public int getApiTimeout() {
        return apiTimeout;
    }

    public void setApiTimeout(int apiTimeout) {
        this.apiTimeout = apiTimeout;
    }
}


