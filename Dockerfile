# Base image with Java 17
FROM eclipse-temurin:17-jdk-alpine AS build

# Working directory
WORKDIR /app

# Copy Maven files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY domain/pom.xml domain/
COPY infrastructure/pom.xml infrastructure/
COPY application/pom.xml application/
COPY api/pom.xml api/

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY domain/src domain/src
COPY infrastructure/src infrastructure/src
COPY application/src application/src
COPY api/src api/src

# Build the application
RUN ./mvnw package -DskipTests

# Final image
FROM eclipse-temurin:17-jre-alpine

# Metadata
LABEL maintainer="Developer <yovanysuarezsilva@gmail.com>"
LABEL version="1.0"
LABEL description="Books and Authors API with Oracle Database"

# Create non-root user to run the application
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the compiled JAR from the build stage
COPY --from=build --chown=spring:spring /app/api/target/*.jar /app/app.jar

# Exposed port
EXPOSE 8080

# Entry point
ENTRYPOINT ["java", "-jar", "/app/app.jar"]