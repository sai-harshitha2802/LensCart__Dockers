FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy JAR
COPY ./target/finalorderUpdated-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8088

ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
