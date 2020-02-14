FROM openjdk:8-jdk as build

RUN apt update

WORKDIR /bot

COPY src src
COPY build.gradle.kts build.gradle.kts
COPY gradle gradle
COPY settings.gradle.kts settings.gradle.kts
COPY gradlew gradlew

RUN gradlew build

FROM openjdk:8-jre-alpine

WORKDIR /bot

COPY --from=build /bot/build/libs/mazen-bot-master.jar bot.jar
CMD java -jar bot.jar
