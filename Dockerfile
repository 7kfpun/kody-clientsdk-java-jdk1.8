FROM openjdk:8-jdk-slim

WORKDIR /app

RUN apt-get update && apt-get install -y maven curl

COPY pom.xml .
COPY src ./src/

RUN mvn package -DskipTests

CMD ["java", "-jar", "target/kodypay-terminal-client-1.0-SNAPSHOT-jar-with-dependencies.jar"]
