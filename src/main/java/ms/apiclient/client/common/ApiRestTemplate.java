package ms.apiclient.client.common;

import java.util.Collections;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import ms.apiclient.model.ApiRequest;
import ms.apiclient.model.ApiResponse;


@Component
public class ApiRestTemplate {

    public ApiResponse callApi(ApiRequest apiReq, String url, int apiTimeout) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(apiTimeout);
        clientHttpRequestFactory.setReadTimeout(apiTimeout);
        restTemplate.setRequestFactory(clientHttpRequestFactory);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<ApiRequest> req = new HttpEntity<>(apiReq, headers);
        try {
            return restTemplate.postForObject(url, req, ApiResponse.class);
        } catch (Exception restEx) {
            throw new Exception("callRestAPISync_" + apiReq.getHeader().getApi(), restEx);
        }
    }
}
