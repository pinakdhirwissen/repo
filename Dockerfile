
FROM openjdk:17-jdk-slim
EXPOSE 8080
ADD ./build/libs/*.jar TicketingSystem-0.0.1-SNAPSHOT.jar
CMD ["java", "-Xmx200m", "-jar", "/TicketingSystem-0.0.1-SNAPSHOT.jar"]