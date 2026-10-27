FROM maven:3.9.11-eclipse-temurin-17 AS build
RUN for alias in \
      "cn_emsign_ecc_root_ca__c3,o_emudhra_inc,ou_emsign_pki,c_us [jdk]" \
      "cn_emsign_ecc_root_ca__g3,o_emudhra_technologies_limited,ou_emsign_pki,c_in [jdk]" \
      "cn_emsign_root_ca__c1,o_emudhra_inc,ou_emsign_pki,c_us [jdk]" \
      "cn_emsign_root_ca__g1,o_emudhra_technologies_limited,ou_emsign_pki,c_in [jdk]"; do \
      if keytool -list -cacerts -storepass changeit -alias "$alias" >/dev/null 2>&1; then \
        keytool -delete -cacerts -storepass changeit -alias "$alias"; \
      fi; \
    done \
    && ! keytool -list -cacerts -storepass changeit | grep -qi emudhra
WORKDIR /workspace
COPY pom.xml ./
COPY contracts/pom.xml contracts/pom.xml
COPY ledger-service/pom.xml ledger-service/pom.xml
COPY projection-service/pom.xml projection-service/pom.xml
COPY contracts/src contracts/src
COPY projection-service/src projection-service/src
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp -pl projection-service -am package -DskipTests
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache --upgrade ca-certificates
RUN for alias in \
      "cn_emsign_ecc_root_ca__c3,o_emudhra_inc,ou_emsign_pki,c_us [jdk]" \
      "cn_emsign_ecc_root_ca__g3,o_emudhra_technologies_limited,ou_emsign_pki,c_in [jdk]" \
      "cn_emsign_root_ca__c1,o_emudhra_inc,ou_emsign_pki,c_us [jdk]" \
      "cn_emsign_root_ca__g1,o_emudhra_technologies_limited,ou_emsign_pki,c_in [jdk]"; do \
      if keytool -list -cacerts -storepass changeit -alias "$alias" >/dev/null 2>&1; then \
        keytool -delete -cacerts -storepass changeit -alias "$alias"; \
      fi; \
    done \
    && ! keytool -list -cacerts -storepass changeit | grep -qi emudhra
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /workspace/projection-service/target/projection-service-*.jar app.jar
USER app
EXPOSE 8082
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","app.jar"]
