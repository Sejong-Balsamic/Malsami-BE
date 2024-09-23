package com.balsamic.sejongmalsami.util.config;

import com.balsamic.sejongmalsami.service.MemberService;
import com.balsamic.sejongmalsami.util.JwtUtil;
import com.balsamic.sejongmalsami.util.filter.TokenAuthenticationFilter;
import java.util.Arrays;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 Spring Security 설정 클래스
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
  private final JwtUtil jwtUtil;
  private final MemberService memberService; // MemberService 주입

  // 인증을 생략할 URL 패턴 목록
  private static final String[] AUTH_WHITELIST = {
      "/", // 기본화면
      "/api/member/signin", // 회원가입
      "/api/login", // 로그인
      "/docs/**", // Swagger
      "/v3/api-docs/**", // Swagger
  };

  // 허용된 CORS Origin 목록
  private static final String[] ALLOWED_ORIGINS = {
      "https://api.sejong-malsami.co.kr",
      "https://www.sejong-malsami.co.kr",
      "http://220.85.169.165:8086",
      "http://220.85.169.165:3002",
      "http://localhost:8080",
      "http://localhost:3000"
  };

  // 웹 보안 무시 설정 (정적 자원 등)
  @Bean
  public WebSecurityCustomizer configure() {
    return (web) -> web.ignoring()
        .requestMatchers(new AntPathRequestMatcher("/static/**"));
  }

  // 보안 필터 체인 설정
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        // CORS 설정
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        // CSRF 비활성화
        .csrf(AbstractHttpConfigurer::disable)
        // HTTP Basic 인증 비활성화
        .httpBasic(AbstractHttpConfigurer::disable)
        // 폼 로그인 비활성화
        .formLogin(AbstractHttpConfigurer::disable)
        // 요청에 대한 권한 설정
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(AUTH_WHITELIST).permitAll()
            // .requestMatchers(HttpMethod.GET, "/api/my-page").hasRole("USER") //FIXME: 예시 페이지
            .anyRequest().authenticated())
        // 로그아웃 설정
        .logout(logout -> logout
            .logoutSuccessUrl("/login")
            .invalidateHttpSession(true)
        )
        // 세션 관리 정책 설정 (STATELESS)
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        // JWT 인증 필터 추가
        .addFilterBefore(new TokenAuthenticationFilter(jwtUtil, memberService, Arrays.asList(AUTH_WHITELIST)),
            UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  // 인증 매니저 설정
  @Bean
  public AuthenticationManager authenticationManager(HttpSecurity http,
      BCryptPasswordEncoder bCryptPasswordEncoder,
      UserDetailsService userDetailsService) throws Exception {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(bCryptPasswordEncoder);
    return new ProviderManager(authProvider);
  }

  // CORS 설정 소스 빈
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(Arrays.asList(ALLOWED_ORIGINS)); // 허용된 Origin 패턴 설정
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")); // 허용된 HTTP 메서드 설정
    configuration.setAllowCredentials(true); // 자격 증명 허용
    configuration.setAllowedHeaders(Collections.singletonList("*")); // 허용된 헤더 설정
    configuration.setMaxAge(3600L); // 캐시 지속 시간 설정 (초 단위)
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 CORS 설정 적용
    return source;
  }

  // 비밀번호 인코더 빈 (BCrypt)
  @Bean
  public BCryptPasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
  }
}
