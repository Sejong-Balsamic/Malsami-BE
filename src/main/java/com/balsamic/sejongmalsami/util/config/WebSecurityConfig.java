package com.balsamic.sejongmalsami.util.config;

import com.balsamic.sejongmalsami.service.MemberService;
import com.balsamic.sejongmalsami.util.JwtUtil;
import com.balsamic.sejongmalsami.util.filter.TokenAuthenticationFilter;
import java.util.Arrays;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
 * Spring Security 설정 클래스
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

  private final JwtUtil jwtUtil;
  private final MemberService memberService;

  // 인증을 생략할 URL 패턴 목록
  private static final String[] AUTH_WHITELIST = {
      "/", // 관리자페이지 메인창
      "/login", // 관리자페이지 로그인창
      "/admin/**", //FIXME: 임시 관리자 페이지 전체허용
      "/api/member/signin", // 회원가입
      "/api/auth/refresh", // 리프레시 토큰
      "/api/course/subjects/faculty", // 교과목명 조회
      "/api/login", // 로그인
      "/api/landing/**", // 랜딩페이지
      "/api/test/**", // 테스트 API
      "/docs/**", // Swagger
      "/v3/api-docs/**", // Swagger
      "/css/**", // CSS 파일
      "/fonts/**", // CSS 파일
      "/images/**", // 이미지 파일
      "/js/**", // JS 파일
      "/robots.txt", // 크롤링 허용 URL 파일
      "/sitemap.xml", // 페이지 URL 파일
      "/favicon.ico" // 아이콘 파일
  };


  // 허용된 CORS Origin 목록
  private static final String[] ALLOWED_ORIGINS = {
      "https://api.sejong-malsami.co.kr",
      "https://api.test.sejong-malsami.co.kr",
      "https://www.sejong-malsami.co.kr",
      "https://test.sejong-malsami.co.kr",
      "http://220.85.169.165:8086",
      "http://220.85.169.165:3002",
      "http://localhost:8080",
      "http://localhost:3000"
  };

  // 보안 필터 체인 설정
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return
        http.cors(cors -> cors
                .configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests((authorize) -> authorize
                .requestMatchers(AUTH_WHITELIST).permitAll()
//                .requestMatchers("/admin/**").hasRole("ADMIN") //FIXME: 임시 전체허용
                .requestMatchers(HttpMethod.POST, "/api/course/upload").hasRole("USER")
                .requestMatchers(HttpMethod.POST, "/api/member/my-page").hasRole("USER")
                .anyRequest().authenticated()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
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
    configuration.setAllowedOriginPatterns(Arrays.asList(ALLOWED_ORIGINS));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    configuration.setAllowCredentials(true);
    configuration.setAllowedHeaders(Collections.singletonList("*"));
    configuration.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  // 비밀번호 인코더 빈 (BCrypt)
  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
