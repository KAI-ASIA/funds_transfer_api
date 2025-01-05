package ms.apiclient.client.common;

import java.util.Collections;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import ms.apiclient.model.ApiRequest;
import ms.apiclient.model.ApiResponse;


@Component
public class ApiRestTemplate {

    public ApiResponse call(String url, ApiRequest request, int timeout) throws RestClientException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<ApiRequest> httpEntity = new HttpEntity<>(request, headers);
        return buildRestTemplate(timeout).postForObject(url, httpEntity, ApiResponse.class);
    }

    private RestTemplate buildRestTemplate(int timeout) {
        RestTemplate restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);
        restTemplate.setRequestFactory(requestFactory);
        return restTemplate;
    }
}
