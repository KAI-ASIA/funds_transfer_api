package com.kaiasia.client.t24util.common;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kaiasia.app.core.model.ApiBody;
import com.kaiasia.app.core.model.ApiError;
import com.kaiasia.app.core.model.ApiHeader;
import com.kaiasia.app.core.model.ApiRequest;
import com.kaiasia.app.core.model.ApiResponse;



@Component
public class CallApiHelper {
	
	
    public static <T> ApiRequest buildENQUIRY(T enquiryInput, ApiHeader header){
        ApiRequest apiReq = new ApiRequest();
        apiReq.setHeader(header); 
        ApiBody apiBody = new ApiBody();
        apiBody.put("COMMAND", "GET_ENQUIRY");
        apiBody.put("ENQUIRY", enquiryInput);
        apiReq.setBody(apiBody);
        return apiReq;
    }
	
    
    public static <T> ApiRequest buildTransaction(T transactionInput, ApiHeader header){
        ApiRequest apiReq = new ApiRequest();
        apiReq.setHeader(header); 
        ApiBody apiBody = new ApiBody();
        apiBody.put("COMMAND", "GET_TRANSACTION");
        apiBody.put("TRANSACTION", transactionInput);
        apiReq.setBody(apiBody);
        return apiReq;
    }
	
	private String url;
	private String apiName;
	private String apiKey;
	private int apiTimeout;
	
	
	@Autowired
    private KaiRestTemplate kaiRestTemplate;
    
    
	 	
	

	 public <T> T commonRest(String location, ApiRequest apiReq, Class<T> classResult) {
	        ApiError apiError = new ApiError();
	        long a = System.currentTimeMillis();
	        try {
	            ApiHeader headerApi = this.rebuildApiHeader(apiReq.getHeader());
	            ApiRequest apiReqRebuild = new ApiRequest();
	            apiReqRebuild.setHeader(headerApi);
	            apiReqRebuild.setBody(apiReq.getBody()); 
	            ApiResponse response = kaiRestTemplate.callApi(apiReqRebuild, apiTimeout, url); 
	            if (response != null && response.getError() != null) {
	                apiError = response.getError();
	                ModelMapper mapper = new ModelMapper();
	                return mapper.map(apiError, classResult);
	            }
	            Map<String, Object> enquiryMap = BaseService.getEnquiry(response);
	            
	            ModelMapper mapper = new ModelMapper();
	             
	            return mapper.map(enquiryMap, classResult);
	        } catch (Exception eis) {
	            apiError.setCode("TIMEOUT");
	            apiError.setDesc(eis.toString());
	            ModelMapper mapper = new ModelMapper();
	            return mapper.map(apiError, classResult);
	        }
	    }

    
    

    private ApiHeader rebuildApiHeader(ApiHeader apiHeader){
        ApiHeader headerApi = new ApiHeader();
        headerApi.setChannel(apiHeader.getChannel());
        headerApi.setSubChannel(apiHeader.getSubChannel());
        headerApi.setContext(apiHeader.getContext());
        headerApi.setLocation(apiHeader.getLocation());
        headerApi.setRequestNode(apiHeader.getRequestNode());
        headerApi.setReqType("REQUEST");
        headerApi.setTrusted(apiHeader.getTrusted());
        headerApi.setUserID(apiHeader.getUserID());
        headerApi.setRequestAPI(apiHeader.getRequestAPI());
        headerApi.setSynasyn("true");
        headerApi.setApi(apiName);
        headerApi.setApiKey(apiKey);
        return headerApi;
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


