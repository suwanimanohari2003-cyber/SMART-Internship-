# ─────────────────────────────────────────────────────────────────────────────
# Stage 1: Build the JAR using Maven
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper + pom.xml first (layer cache: deps only re-downloaded on pom change)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached layer)
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copy source and build
COPY src src
RUN ./mvnw package -DskipTests -B

# ─────────────────────────────────────────────────────────────────────────────
# Stage 2: Lightweight runtime image
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create uploads directory (writable by app)
RUN mkdir -p /tmp/uploads/cvs

# Copy the built JAR from stage 1
COPY --from=builder /build/target/*.jar app.jar

# Expose port (Railway will set PORT env var)
EXPOSE 8080

# Run the app
# NOTE: DB_URL, DB_USERNAME, DB_PASSWORD must be set as environment variables
#       in your cloud platform (Railway / Cloud Run / Docker run -e flags).
#       The hibernate.dialect is pinned here so Hibernate never tries to
#       auto-detect it (which requires a live JDBC connection at startup).
ENTRYPOINT ["java", \
            "-Dserver.port=${PORT:-8080}", \
            "-Dfile.upload-dir=/tmp/uploads/cvs", \
            "-Dspring.thymeleaf.cache=true", \
            "-Dspring.jpa.database-platform=org.hibernate.dialect.MySQLDialect", \
            "-Dspring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect", \
            "-jar", "app.jar"]
