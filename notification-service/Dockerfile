FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# On prend le JAR qui a été compilé sur ton Windows
COPY target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]