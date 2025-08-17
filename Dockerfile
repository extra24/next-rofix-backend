FROM openjdk:17-jre-slim

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 컨텍스트 확인을 위한 디버깅 (임시)
RUN echo "=== Docker build context check ===" && ls -la /

# JAR 파일 존재 확인 후 복사
COPY build/libs/webservice-0.0.1-SNAPSHOT.jar fitspot.jar

# 복사 확인
RUN echo "=== Copied files check ===" && ls -la /app/

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "fitspot.jar"]