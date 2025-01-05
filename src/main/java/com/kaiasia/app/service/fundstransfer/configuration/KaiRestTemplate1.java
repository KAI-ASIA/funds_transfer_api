package com.kaiasia.app.service.fundstransfer.configuration;

import com.kaiasia.app.core.model.ApiRequest;
import com.kaiasia.app.core.model.ApiResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Component
public class KaiRestTemplate1 {
//    @Autowired
//    private RestTemplateBuilder builder;

    public ApiResponse call(String url, ApiRequest request, int timeout) {
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
