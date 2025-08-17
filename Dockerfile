FROM openjdk:17-jre-slim

# 작업 디렉토리 설정
WORKDIR /app

# JAR 파일이 있는지 확인하고 복사
# 빌드 컨텍스트에서 파일 확인을 위한 디버깅
RUN echo "Checking build context..." && ls -la /

# JAR 파일 복사
COPY build/libs/webservice-0.0.1-SNAPSHOT.jar fitspot.jar

# 복사된 파일 확인
RUN ls -la /app/

# 포트 노출 (Spring Boot 기본 포트)
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "fitspot.jar"]