#define base docker image
FROM openjdk:11
ADD target/node-1.0-SNAPSHOT.jar node.jar
ENTRYPOINT ["java", "-jar", "node.jar"]
