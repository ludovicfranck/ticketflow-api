# Étape 1 : Construction (Maven)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
#  le JAR est créé à l'intérieur de Docker
RUN mvn clean package -DskipTests

# Étape 2 : Image finale légère (JRE)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# On récupère le JAR généré à l'étape précédente
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]