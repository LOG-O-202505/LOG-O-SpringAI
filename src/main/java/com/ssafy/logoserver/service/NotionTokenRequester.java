package com.ssafy.logoserver.service;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

// request token을 생성해주는 클래스
public class NotionTokenRequester {
    private final RestTemplate restTemplate;
    private final HttpHeaders headers;
    private final MultiValueMap<String, String> requestBody;

    public NotionTokenRequester(String clientId, String clientSecret) {
        restTemplate = new RestTemplate();
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret); // Basic 인증 헤더 설정
        requestBody = new LinkedMultiValueMap<>();
    }

    public void addParameter(String key, String value) {
        requestBody.add(key, value);
    }

    public String requestToken(String endpoint) {
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(endpoint, HttpMethod.POST, requestEntity, String.class);
        return responseEntity.getBody();
    }
}
