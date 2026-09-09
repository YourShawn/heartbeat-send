FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /src
COPY pom.xml .
COPY src src
RUN apt-get update && apt-get install -y --no-install-recommends maven \
    && mvn -q -DskipTests package \
    && rm -rf /root/.m2

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /src/target/heartbeat-send-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
