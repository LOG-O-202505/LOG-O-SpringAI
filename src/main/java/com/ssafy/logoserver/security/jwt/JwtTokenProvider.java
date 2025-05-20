package com.ssafy.logoserver.security.jwt;

import com.ssafy.logoserver.domain.user.dto.TokenDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final String AUTHORITIES_KEY = "auth";
    private static final String GRANT_TYPE = "Bearer";
    private final Key key;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity}") long accessTokenValidity,
            @Value("${jwt.refresh-token-validity}") long refreshTokenValidity
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidity = accessTokenValidity * 1000;
        this.refreshTokenValidity = refreshTokenValidity * 1000;
    }

    /**
     * AccessToken만을 생성하여 반환
     * (TokenDto가 아닌 실제 Jwts 타입의 토큰을 반환)
     * */
    public String createAccessToken(Authentication authentication) {
        log.info("let's create accessToken start");
        return createToken(authentication, accessTokenValidity);
    }

    /**
     * refreshToken만을 생성하여 반환
     * (TokenDto가 아닌 실제 Jwts 타입의 토큰을 반환)
     * */
    public String createRefreshToken(Authentication authentication) {
        log.info("let's create refreshToken start");
        return createToken(authentication, refreshTokenValidity);
    }

    /**
     * 실제 토큰 생성 메서드
     * 생성이 완료된 토큰을 반환
     * */
    private String createToken(Authentication authentication, long validity) {
        log.info("generateToken start");
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date expiration = new Date(now + validity);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(expiration)
                .compact();
    }

    /**
     * AccessToken, RefreshToken 둘 다 생성(회원가입 시, RefreshToken 만료 시)
     * 완성된 토큰을 담는 TokenDto를 반환
     * */
    public TokenDto generateToken(Authentication authentication) {
        return TokenDto.builder()
                .grantType(GRANT_TYPE)
                .accessToken(createAccessToken(authentication))
                .refreshToken(createRefreshToken(authentication))
                .build();
    }

    /**
     * 토큰을 통해 유저 정보 추출
     * */
    public Authentication getAuthentication(String token) {
        log.info("[start token getAuthentication]");
        Claims claims = parseToken(token).getBody();

        log.info("claims: {}", claims);
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        log.info("authorities: {}", authorities);
        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * 토큰 유효 여부 검증
     * */
    public boolean validateToken(String token) {
        log.info("[validateToken start]");
        log.info("token: {}", token);
        try {
            parseToken(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            // refresh token 활용해서 재발급
            log.info("Expired JWT Token: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    private Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}