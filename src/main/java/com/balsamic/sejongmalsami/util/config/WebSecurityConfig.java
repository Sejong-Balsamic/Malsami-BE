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
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * 웹 보안 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

  private final JwtUtil jwtUtil;
  private final MemberService memberService;

  /**
   * 허용된 CORS Origin 목록
   */
  private static final String[] ALLOWED_ORIGINS = {
      "https://api.sejong-malsami.co.kr",      // 운영 API 서버
      "https://api.test.sejong-malsami.co.kr", // 테스트 API 서버
      "https://www.sejong-malsami.co.kr",      // 운영 웹 서버
      "https://test.sejong-malsami.co.kr",     // 테스트 웹 서버
      "http://220.85.169.165:8086",            // 개발 API 서버
      "http://220.85.169.165:3002",            // 개발 웹 서버
      "http://localhost:8080",                 // 로컬 API 서버
      "http://localhost:3000"                  // 로컬 웹 서버
  };

  /**
   * 보안 필터 체인 설정
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests((authorize) -> authorize
            .requestMatchers(SecurityUrls.AUTH_WHITELIST.toArray(new String[0])).permitAll()
            .requestMatchers(SecurityUrls.ADMIN_PATHS.toArray(new String[0])).hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/course/upload").hasRole("USER")
            .requestMatchers(HttpMethod.POST, "/api/member/my-page").hasRole("USER")
            .requestMatchers(HttpMethod.POST, "/api/question/post").hasRole("USER")
            .requestMatchers(HttpMethod.POST, "/api/question/get").hasRole("USER")
            .requestMatchers(HttpMethod.POST, "/api/question/get/all").hasRole("USER")
            .requestMatchers(HttpMethod.POST, "/api/question/get/unanswered").hasRole("USER")
            .requestMatchers(HttpMethod.POST, "/api/question/get/filtered-posts").hasRole("USER")
            .requestMatchers(HttpMethod.POST, "/api/question/popular/daily").hasRole("USER")
            .requestMatchers(HttpMethod.POST, "/api/question/popular/weekly").hasRole("USER")
            .anyRequest().authenticated()
        )
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login")
            .invalidateHttpSession(true)
            .deleteCookies("refreshToken")
        )
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .addFilterBefore(
            new TokenAuthenticationFilter(jwtUtil, memberService),
            UsernamePasswordAuthenticationFilter.class
        )
        .build();
  }

  /**
   * 인증 매니저 설정
   */
  @Bean
  public AuthenticationManager authenticationManager(
      HttpSecurity http,
      BCryptPasswordEncoder bCryptPasswordEncoder,
      UserDetailsService userDetailsService
  ) throws Exception {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(bCryptPasswordEncoder);
    return new ProviderManager(authProvider);
  }

  /**
   * CORS 설정 소스 빈
   */
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

  /**
   * 비밀번호 인코더 빈 (BCrypt)
   */
  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
