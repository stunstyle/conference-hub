# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /home/app
COPY pom.xml .
# Download dependencies first to cache them
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Package the runtime container
FROM eclipse-temurin:21-jre-jammy
WORKDIR /deployments
COPY --from=build /home/app/target/quarkus-app/ .
EXPOSE 8080
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENTRYPOINT [ "java", "-jar", "quarkus-run.jar" ]
