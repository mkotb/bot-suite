FROM gradle:4.7.0-jdk8-alpine as build

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM openjdk:8-jre-alpine

WORKDIR /bot

COPY --from=build /home/gradle/src/build/libs/mazen-bot-master.jar bot.jar
CMD java -jar bot.jar
