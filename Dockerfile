FROM openjdk:17-slim
WORKDIR /app
COPY build/libs/webservice-0.0.1-SNAPSHOT.jar fitspot.jar
ENTRYPOINT ["java", "-jar", "fitspot.jar"]