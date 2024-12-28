package com.kaiasia.app.service.fundstransfer.utils;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Slf4j
public class ApiCallHelper<T> {
    private String url;
    private HttpMethod httpMethod;
    private String body;
    private HttpHeaders headers;
    private Class<T> responseType;

    public static <T> T call(String url, Class<T> responseType) {
        return call(url, HttpMethod.GET, responseType);
    }

    public static <T> T call(String url, HttpMethod httpMethod, Class<T> responseType) {
        return call(url, httpMethod, "", responseType);
    }

    public static <T> T call(String url, HttpMethod httpMethod, String body, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentLength(body.getBytes().length);
        return call(url, httpMethod, body, headers, responseType);
    }

    public static <T> T call(String url, HttpMethod httpMethod, String body, MultiValueMap<String, String> headers, Class<T> responseType) {
        RestTemplate restTemplate = new RestTemplate();
        RequestEntity<String> entity = new RequestEntity<>(body, headers, httpMethod, URI.create(url));
        ResponseEntity<T> response = restTemplate.exchange(entity, responseType);
        T apiResponse = response.getBody();
        log.info("Successfully executed api call: {}", apiResponse);
        return apiResponse;
    }

    public T call() {
        return call(url, httpMethod, body, headers, responseType);
    }

    public static <T> ApiCallBuilder<T> builder() {

        return new ApiCallBuilder<T>();
    }

    public static class ApiCallBuilder<Y> {
        ApiCallHelper<Y> apiCallHelper;

        private ApiCallBuilder() {
            apiCallHelper = new ApiCallHelper<Y>();
            apiCallHelper.httpMethod = HttpMethod.GET;
            apiCallHelper.body = "";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            apiCallHelper.headers = headers;
        }

        public ApiCallBuilder<Y> url(String url) {
            this.apiCallHelper.url = url;
            return this;
        }

        public ApiCallBuilder<Y> httpMethod(HttpMethod httpMethod) {
            this.apiCallHelper.httpMethod = httpMethod;
            return this;
        }

        public ApiCallBuilder<Y> body(String body) {
            this.apiCallHelper.body = body;
            return this;
        }

        public ApiCallBuilder<Y> headers(HttpHeaders headers) {
            this.apiCallHelper.headers = headers;
            return this;
        }

        public ApiCallBuilder<Y> responseType(Class<Y> responseType) {
            this.apiCallHelper.responseType = responseType;
            return this;
        }

        public ApiCallHelper<Y> build() {
            return apiCallHelper;
        }
    }
}
