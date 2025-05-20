package com.ssafy.logoserver.security.oauth;

import com.ssafy.logoserver.domain.user.dto.TokenDto;
import com.ssafy.logoserver.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        //OAuth2 user
        CustomOauth2User customUserDetails = (CustomOauth2User) authentication.getPrincipal();

        String userid = customUserDetails.getName();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();

        TokenDto tokenDto = jwtTokenProvider.generateToken(authentication);
        response.addHeader("Authorization", "Bearer " + tokenDto.getAccessToken());
        response.addCookie(createCookie("RefreshToken", tokenDto.getRefreshToken()));
        response.sendRedirect("http://localhost:3000/");
    }

    private Cookie createCookie(String key, String token) {
        Cookie cookie = new Cookie(key, token);
        cookie.setMaxAge(60*60*60);
//        cookie.setSecure(true); //쿠키가 https 환경에서만 주고받을지(개발시엔 주석처리)
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}
