FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY contracts/pom.xml contracts/pom.xml
COPY ledger-service/pom.xml ledger-service/pom.xml
COPY projection-service/pom.xml projection-service/pom.xml
COPY contracts/src contracts/src
COPY projection-service/src projection-service/src
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp -pl projection-service -am package -DskipTests
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /workspace/projection-service/target/projection-service-*.jar app.jar
USER app
EXPOSE 8082
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","app.jar"]
