# Multi-stage Docker build for SpendWise
# Stage 1: Build the application
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /workspace

# Copy maven wrapper and pom.xml
COPY .mvn .mvn
COPY mvnw pom.xml ./

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code and build jar
COPY src src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime environment
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a non-privileged user
RUN addgroup -S spendwise && adduser -S spendwise -G spendwise

# Copy jar from builder stage
COPY --from=builder /workspace/target/*.jar app.jar

# Create directory for persistent H2 database file
RUN mkdir -p /app/data && chown -R spendwise:spendwise /app

USER spendwise

# Expose default port
EXPOSE 8080

# Environment variables
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod

# Launch Spring Boot
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
