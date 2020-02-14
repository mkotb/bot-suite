FROM openjdk:8-jre-alpine

WORKDIR /bot

COPY ./build/libs/mazen-bot-master.jar bot.jar
CMD java -jar bot.jar
