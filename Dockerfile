# Utilisation de l'image de runtime légère pour Java 21
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copie du jar généré (assure-toi que ton pom.xml génère bien un jar dans /target)
COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]