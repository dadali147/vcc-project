FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests -pl vcc-admin -am

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/vcc-admin/target/vcc-admin.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
