package com.balsamic.sejongmalsami.util.config;

import java.util.Arrays;
import java.util.List;

/**
 * Security 관련 URL 상수 관리
 */
public class SecurityUrls {

  /**
   * 인증을 생략할 URL 패턴 목록
   */
  public static final List<String> AUTH_WHITELIST = Arrays.asList(
      // API
      "/api/auth/refresh",             // 리프레시 토큰
      "/api/course/subjects/faculty",   // 교과목명 조회
      "/api/login",                    // 로그인
      "/api/landing/**",               // 랜딩페이지
      "/api/test/**",                  // 테스트 API
      "/api/member/signin",            // 회원가입
      "/admin/auth/login",          // 관리자 로그인 API

      // Swagger
      "/docs/**",                      // Swagger UI
      "/v3/api-docs/**",              // Swagger API 문서

      // WEB
      "/",                             // 관리자페이지 메인창
      "/login",                        // 관리자페이지 로그인창
      "/error/**",                        // 에러 페이지

      // Static Resources
      "/css/**",                       // CSS 파일
      "/fonts/**",                     // 폰트 파일
      "/images/**",                    // 이미지 파일
      "/js/**",                        // JS 파일

      // SEO
      "/robots.txt",                   // 크롤링 허용 URL 파일
      "/sitemap.xml",                  // 페이지 URL 파일
      "/favicon.ico"                   // 아이콘 파일
  );

  /**
   * 관리자 권한이 필요한 URL 패턴 목록
   */
  public static final List<String> ADMIN_PATHS = Arrays.asList(
      "/admin/dashboard",                     // 관리자 페이지
      "/admin/testPage1"

  );
}