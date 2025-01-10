FROM eclipse-temurin:21-jdk AS build
RUN apt-get update && apt-get install -y maven
WORKDIR /usr/src/app
COPY . .
RUN mvn package

FROM eclipse-temurin:21-jre
COPY --from=build /usr/src/app/target/*-runner.jar /work/application.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/work/application.jar", "-Dquarkus.http.host=0.0.0.0"]
