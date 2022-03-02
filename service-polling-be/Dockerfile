FROM gradle:7.4-jdk17 as build

RUN mkdir /code
COPY . /code
WORKDIR /code

RUN gradle build

FROM openjdk:17

RUN mkdir /app
COPY --from=build /code/build/libs/service-polling-be-1.0.0-SNAPSHOT-fat.jar /app/sp.jar

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-jar","/app/sp.jar"]