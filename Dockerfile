# =============================================
# Stage 1: Build
# Use Maven + JDK 21 to compile and package the app
# =============================================
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml first to cache dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build (skip tests in Docker build)
COPY src ./src
RUN mvn package -DskipTests -q

# =============================================
# Stage 2: Runtime
# Use slim JRE image for smaller final image
# =============================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy only the built jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose Spring Boot default port
EXPOSE 8080

# Environment variables (overridable via docker-compose)
ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysqldb:3306/inventory_db
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=1234
ENV JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
ENV JWT_EXPIRATION=86400000

ENTRYPOINT ["java", "-jar", "app.jar"]
