FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# COPY dist/scm2.0-0.0.1-SNAPSHOT.jar /app/scm2.0-0.0.1-SNAPSHOT.jar
COPY target/0-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "scm2.0-0.0.1-SNAPSHOT.jar"]
