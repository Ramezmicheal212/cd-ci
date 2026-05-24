FROM eclipse-temurin:25-jre

WORKDIR /app
COPY  target/*.jar hotel.jar
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/hotel.jar"]
