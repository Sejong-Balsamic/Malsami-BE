FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일과 application-prod.yml을 복사
COPY build/libs/sejong-malsami-backend-0.0.1-SNAPSHOT.jar /app.jar
COPY src/main/resources/application-prod.yml /app/resources/application-prod.yml

# 애플리케이션 실행 시 프로파일 설정 파일 경로를 명시
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.config.location=classpath:/app/resources/application-prod.yml"]

# Spring Boot 서버 포트 노출
EXPOSE 8080
