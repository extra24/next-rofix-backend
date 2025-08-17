FROM openjdk:17-jre-slim

# 작업 디렉토리 설정
WORKDIR /app

# JAR 파일 복사 (빌드된 파일명과 정확히 일치시켜야 함)
COPY build/libs/webservice-0.0.1-SNAPSHOT.jar fitspot.jar

# 포트 노출 (Spring Boot 기본 포트, 필요에 따라 변경)
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "fitspot.jar"]