# Docker packages the build tool and runtime so deployments do not depend on
# locally installed Maven or Java versions.
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S skincare && adduser -S skincare -G skincare
COPY --from=builder --chown=skincare:skincare /workspace/target/skincare-backend-*.jar app.jar

USER skincare
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
