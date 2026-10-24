# ==========================================
# Multi-Stage Dockerfile for KUET UMS
# ==========================================

# Stage 1: Build Application
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /build

# Copy Maven wrapper and POM first for layer caching
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and build production jar
COPY src src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime Image
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Create non-root system user for security
RUN addgroup -S kuet && adduser -S kuet -G kuet

# Copy executable jar from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Change ownership to non-root user
RUN chown kuet:kuet app.jar

USER kuet:kuet

EXPOSE 8088

# Container healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8088/ || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
