#define base docker image
FROM openjdk:11
ADD target/dispatcher-1.0-SNAPSHOT.jar dispatcher.jar
ENTRYPOINT ["java", "-jar", "dispatcher.jar"]
