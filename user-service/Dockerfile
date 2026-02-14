FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY gradle/wrapper gradle/wrapper
COPY gradlew .
COPY settings.gradle.kts .
COPY user-service ./user-service
RUN ./gradlew :user-service:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-jammy AS final
WORKDIR /app
EXPOSE 8080
COPY --from=builder /app/user-service/build/libs/*-boot.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]