FROM openjdk:11-jdk-slim
MAINTAINER My Company (it@mycompany.com)

ARG JAR_FILE
ARG JAVA_AGENT_URL=""
ARG JAVA_AGENT_FILE=""
ENV JAVA_OPTS=""

RUN apt-get update; apt-get install -y curl
COPY $JAR_FILE /app/app.jar
ADD $JAVA_AGENT_URL /agents/
COPY $JAVA_AGENT_FILE /agents/

ENTRYPOINT exec java $JAVA_OPTS -jar /app/app.jar