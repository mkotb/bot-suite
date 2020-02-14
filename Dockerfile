FROM openjdk:11.0-slim

WORKDIR /bot

COPY ./build/libs/mazen-bot-master.jar bot.jar
CMD java -jar bot.jar
