FROM adoptopenjdk/openjdk11:alpine-jre
WORKDIR /app
ADD ./build/libs/onlyagricare-assignment-1.0.0.jar /app/onlyagricare-assignment-1.0.0.jar
ENTRYPOINT ["java", "-jar", "/app/onlyagricare-assignment-1.0.0.jar"]
