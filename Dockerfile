FROM openjdk:19-jdk-alpine
WORKDIR /opt/app
ARG JAR_FILE=target/tui-homework-0.0.1.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]