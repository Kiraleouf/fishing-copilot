# Fishing Copilot

Ce projet fournit une base avec un backend Spring Boot en Kotlin utilisant Maven, Flyway et Hibernate configuré pour PostgreSQL. Un frontend en JavaScript natif est servi depuis le dossier `static` et est accessible via l'URL du backend.

## Démarrage

Pour lancer l'application :

```
./mvnw spring-boot:run
```

L'interface statique est disponible sur `http://localhost:8080/`.

## Documentation API

Une documentation OpenAPI est disponible après le démarrage de l'application :

- Swagger UI : `http://localhost:8080/swagger-ui.html`
- Spécification JSON : `http://localhost:8080/v3/api-docs`

### Endpoints

| Méthode | Chemin | Description |
|---------|--------|-------------|
| POST | `/register` | Enregistre un nouveau pêcheur |
| POST | `/sign-in` | Authentifie un pêcheur |
| GET | `/fisherman/{login}/secret-question` | Récupère la question secrète ou vérifie la réponse |
| PATCH | `/fisherman/{login}/password` | Met à jour le mot de passe après validation de la réponse secrète |
