FROM eclipse-temurin:25-jre

WORKDIR /app
COPY  demo-0.0.1-SNAPSHOT.jar hotel.jar
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/hotel.jar"]
