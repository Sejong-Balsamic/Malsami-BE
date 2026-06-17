FROM eclipse-temurin:17-jdk

# 작업 디렉토리 설정
WORKDIR /app

# JAR 파일 복사 (SM-Web 모듈 부트 JAR, 버전 무관 고정명)
COPY SM-Web/build/libs/SM-Web.jar /app.jar

# 애플리케이션 실행 (기본 Spring Boot 설정)
ENTRYPOINT ["java", "-jar", "/app.jar"]

# Spring Boot 서버 포트 노출
EXPOSE 8080
