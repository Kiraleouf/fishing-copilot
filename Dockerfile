# Étape 1 : Build du front
FROM node:20 AS frontend-build
WORKDIR /frontend
COPY frontend/ .
RUN npm install && npm run build

# Étape 2 : Build du back
FROM maven:3.9.6-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Étape 3 : Image finale
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copier le jar
COPY --from=backend-build /app/target/*.jar app.jar

# Copier le front dans static/ pour être servi par Spring Boot
COPY --from=frontend-build /frontend/dist /app/static

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
