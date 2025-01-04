package ms.apiclient.client.common;

import java.util.Collections;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import ms.apiclient.app.core.model.ApiRequest;
import ms.apiclient.app.core.model.ApiResponse;


@Component
public class ApiRestTemplate {

	 public ApiResponse callApi(ApiRequest apiReq, String url, int apiTimeout) throws Exception {
		 RestTemplate  restTemplate = new RestTemplate();
		 HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(apiTimeout);
        clientHttpRequestFactory.setReadTimeout(apiTimeout);
        restTemplate.setRequestFactory(clientHttpRequestFactory);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<ApiRequest> req = new HttpEntity<ApiRequest>(apiReq, headers);
        ResponseEntity<ApiResponse> result;
        try {
            result = restTemplate.postForEntity(url, req, ApiResponse.class);
        } catch (Exception restEx) {
            throw new Exception("callRestAPISync_" + apiReq.getHeader().getApi(), restEx);
        }
        ApiResponse apiRes = result.getBody();
        return apiRes;
 }
	
	
	
}
