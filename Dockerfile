FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# JAR 파일 복사 (SM-Web 모듈 JAR)
COPY SM-Web/build/libs/SM-Web-0.0.1-SNAPSHOT.jar /app.jar

# 애플리케이션 실행 (기본 Spring Boot 설정)
ENTRYPOINT ["java", "-jar", "/app.jar"]

# Spring Boot 서버 포트 노출
EXPOSE 8080
