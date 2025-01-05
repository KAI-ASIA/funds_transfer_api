package ms.apiclient.client.common;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ms.apiclient.model.ApiBody;
import ms.apiclient.model.ApiError;
import ms.apiclient.model.ApiHeader;
import ms.apiclient.model.ApiRequest;
import ms.apiclient.model.ApiResponse;
import org.springframework.web.client.RestClientException;

@Component
@Slf4j
public abstract class ApiCallClient {
    private static final ModelMapper mapper = new ModelMapper();
    @Autowired
    private ApiRestTemplate apiRestTemplate;
    private String url;
    private String apiName;
    private String apiKey;
    private int apiTimeout;

    protected <T> T call(String location, ApiRequest request, Class<T> classResult) throws RestClientException {
        request.setHeader(rebuildHeader(request.getHeader()));
        log.info("{}#begin call api {}", location, apiName);
        ApiResponse response;
        try {
            response = apiRestTemplate.call(url, request, apiTimeout);
        } catch (RestClientException e) {
            log.error("{}#while calling api {}: {}", location, apiName, e.getMessage());
            throw e;
        }
        log.info("{}#end call api {}", location, apiName);

        ApiError apiError;
        if (response == null) {
            apiError = new ApiError("999", "Unknown Response");
            return mapper.map(apiError, classResult);
        }
        if (response.getError() != null) {
            apiError = response.getError();
            return mapper.map(apiError, classResult);
        }

        Map<String, Object> enquiryOrTransaction = getEnquiry(response);
        if (enquiryOrTransaction == null) {
            enquiryOrTransaction = getTransaction(response);
        }

        return mapper.map(enquiryOrTransaction, classResult);
    }


    private ApiHeader rebuildHeader(ApiHeader header) {
        header.setApi(apiName);
        header.setApiKey(apiKey);
        return header;
    }

    public Map<String, Object> getTransaction(ApiResponse response) {
        return (Map<String, Object>) response.getBody().get("transaction");
    }

    public static Map<String, Object> getEnquiry(ApiResponse response) {
        return (Map<String, Object>) response.getBody().get("enquiry");
    }

    public static <T> ApiRequest buildENQUIRY(T enquiryInput, ApiHeader header) {
        ApiRequest apiReq = new ApiRequest();
        apiReq.setHeader(header);
        ApiBody apiBody = new ApiBody();
        apiBody.put("command", "GET_ENQUIRY");
        apiBody.put("enquiry", enquiryInput);
        apiReq.setBody(apiBody);
        return apiReq;
    }


    public static <T> ApiRequest buildTransaction(T transactionInput, ApiHeader header) {
        ApiRequest apiReq = new ApiRequest();
        apiReq.setHeader(header);
        ApiBody apiBody = new ApiBody();
        apiBody.put("command", "GET_TRANSACTION");
        apiBody.put("transaction", transactionInput);
        apiReq.setBody(apiBody);
        return apiReq;
    }
}


