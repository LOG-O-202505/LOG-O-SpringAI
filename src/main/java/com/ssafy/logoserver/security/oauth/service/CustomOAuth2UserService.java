package com.ssafy.logoserver.security.oauth.service;

import com.ssafy.logoserver.domain.user.entity.User;
import com.ssafy.logoserver.domain.user.repository.UserRepository;
import com.ssafy.logoserver.security.oauth.CustomOauth2User;
import com.ssafy.logoserver.security.oauth.dto.GoogleResponse;
import com.ssafy.logoserver.security.oauth.dto.NaverResponse;
import com.ssafy.logoserver.security.oauth.dto.OAuth2Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        log.info("getAttributes : {}",oAuth2User.getAttributes());

        //구글인지, 네이번지, 카카오인지 알려줄놈
        String provider = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Response oAuth2Response = null;

        // 뒤에 진행할 다른 소셜 서비스 로그인을 위해 구분 => 구글
        if(provider.equals("google")){
            log.info("구글 로그인");
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());

        }else if(provider.equals("naver")){
            log.info("네이버 로그인");
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());

        }else return null;

        String providerId = oAuth2Response.getProviderId();
        String email = oAuth2Response.getEmail();
        String loginId = oAuth2Response.getProvider() + "_" + providerId;
        String name = oAuth2Response.getName();

        User findUser = userRepository.findById(loginId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자 ID가 존재하지 않습니다: " + loginId));
        User user;

        if (findUser == null) {
            user = User.builder()
                    .id(loginId)
                    .name(name)
                    .email(email)
                    .provider(provider)
                    .providerId(providerId)
                    .role(User.Role.USER)
                    .build();
            userRepository.save(user);
        } else{
            user = findUser;
        }

        return new CustomOauth2User(user, oAuth2User.getAttributes());
    }
}
