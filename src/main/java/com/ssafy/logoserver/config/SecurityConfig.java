package com.ssafy.logoserver.config;

import com.ssafy.logoserver.security.jwt.JwtCookieProvider;
import com.ssafy.logoserver.security.jwt.JwtFilter;
import com.ssafy.logoserver.security.jwt.JwtTokenProvider;
import com.ssafy.logoserver.security.jwt.TokenRotationService;
import com.ssafy.logoserver.security.oauth2.CustomOAuth2UserService;
import com.ssafy.logoserver.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.ssafy.logoserver.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.ssafy.logoserver.security.oauth2.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRotationService tokenRotationService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtCookieProvider jwtCookieProvider) throws Exception {
        //csrf 공격 방어 설정
        http
                .csrf(AbstractHttpConfigurer::disable);
                // CSRF는 쿠키 사용하므로 활성화 (주의: REST API에 맞게 설정)
//                .csrf(csrf -> csrf
//                        .ignoringRequestMatchers("/api/auth/login", "/api/auth/signup", "/api/auth/logout")
//                );

        //cors 방지 설정
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));


        //HTTP Basic 인증 방식 disable
        http
                .httpBasic(AbstractHttpConfigurer::disable);

        //세션 설정
        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // OAuth2 로그인 설정
        http
                .oauth2Login(oauth2 -> oauth2
                        // 기본 엔드포인트를 사용하므로 authorizationEndpoint 설정이 선택적
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository))
                        // 기본 리다이렉트 엔드포인트를 사용하므로 redirectionEndpoint 설정 불필요
                        .userInfoEndpoint(endpoint -> endpoint
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                );


        http
                .authorizeHttpRequests(auth -> auth
                        // API 문서 접근 허용
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // AI 관련 API는 인증 없이 접근 가능
                        .requestMatchers("/api/chat/**").permitAll()
                        // 회원가입과 로그인, 헬스체크는 인증 없이 접근 가능
                        .requestMatchers("/api/auth/**", "/health").permitAll()
                        // 뷰 템플릿 접근 허용
                        .requestMatchers("/", "/login", "/signup", "/error", "/mypage").permitAll()
                        // 정적 리소스 접근 허용
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        // 나머지 API는 인증 필요
                        .requestMatchers("/api/**").authenticated()
//                        .requestMatchers("/api/**").permitAll()
                        // 관리자 API는 ADMIN 역할 필요
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                );



        // 커스텀 JWT 필터 추가
        http.addFilterBefore(new JwtFilter(jwtTokenProvider, tokenRotationService, jwtCookieProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:8080", "http://localhost:8090"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true); // 쿠키 전송을 위해 필요

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}