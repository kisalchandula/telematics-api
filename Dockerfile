FROM eclipse-temurin:21-jre

WORKDIR /app

COPY build/libs/telematics-api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
EXPOSE 5000

ENTRYPOINT ["java", "-jar", "app.jar"]