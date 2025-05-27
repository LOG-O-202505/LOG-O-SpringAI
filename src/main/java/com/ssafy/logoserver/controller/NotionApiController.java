package com.ssafy.logoserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.logoserver.service.NotionTokenRequester;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class NotionApiController {

    private static final String callBackUrl = "http://localhost:8080/api/notion/test"; // redirection url 을 입력해주세요
    private static final String clientId = "1f2d872b-594c-8040-b9b5-003752b84140";//clientId 를 입력해주세요
    private static final String authorizeUrl = "https://api.notion.com/v1/oauth/authorize?owner=user";
    private static final String clientPw = "secret_fwUNJjWfe11gfkMJyQ8lyK7SvFJorljByijki2uUCgq";//clientPw 를 입력해주세요

    // 인증 url 을 생성해주는 메소드
    private static String getAuthGrantType(String callbackURL) {
        return authorizeUrl + "&client_id=" + clientId + "&redirect_uri=" + callbackURL + "&response_type=code";
    }

    //노션 인증 URL 접속(생략 가능)
    @GetMapping("/api/notion/auth")
    public static String Test(){
        String authGrantType = getAuthGrantType(callBackUrl);
        return "redirect:" + authGrantType;
    }

    //리다이렉션 url로부터 code를 받아와 json 을 요청하여 액세스토큰을 받는 메소드
    @GetMapping("/api/notion/test")
    @ResponseBody
    public String handleCallback(@RequestParam("code") String code) throws JsonProcessingException {
        // Do something with the code
        System.out.println("Authorization Code: " + code);

        NotionTokenRequester requester = new NotionTokenRequester(clientId, clientPw);
        requester.addParameter("grant_type", "authorization_code");
        requester.addParameter("code", code);
        requester.addParameter("redirect_uri", callBackUrl);

        String response = requester.requestToken("https://api.notion.com/v1/oauth/token");

        System.out.println(response);
        Map<String, Object> jsonMap = new ObjectMapper().readValue(response, new TypeReference<Map<String, Object>>() {});
        String accessToken = (String) jsonMap.get("access_token");


        System.out.println("Access Token: " + accessToken);

        // Redirect to another page
        return accessToken;
    }
}
