# TicketFlow — Système de Gestion de Tickets Support
## Author Fogang Kamdjouong Franck Ludovic

> Architecture microservices Java Spring Boot — BOAZ-STUDY Test Technique v1.1

---

## Table des matières

1. [Architecture](#architecture)
2. [Prérequis](#prérequis)
3. [Démarrage rapide](#démarrage-rapide)
4. [Modèle de sécurité RBAC+ABAC](#modèle-de-sécurité-rbacabac)
5. [Endpoints par service](#endpoints-par-service)
6. [Tester avec Postman / curl](#tester-avec-postman--curl)
7. [URLs de référence](#urls-de-référence)
8. [Architecture technique détaillée](#architecture-technique-détaillée)

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     CLIENT (Postman / Frontend)              │
└─────────────────────────┬───────────────────────────────────┘
                          │ HTTP :8080
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                  API GATEWAY (port 8080)                      │
│  • Validation JWT (Keycloak JWKS)                            │
│  • Routing vers les microservices                            │
│  • CORS headers                                              │
│  • Swagger Aggregator                                        │
└──────┬──────────┬──────────┬──────────┬──────────────────────┘
       │          │          │          │
       ▼          ▼          ▼          ▼
  user-svc  ticket-svc  notif-svc  doc-svc
  :8081      :8082       :8083      :8084
       │          │          │          │
       └──────────┴────┬──────┴──────────┘
                       │
          ┌────────────▼────────────┐
          │    KAFKA (9092)          │
          │  Topics:                 │
          │  • user.created          │
          │  • ticket.created        │
          │  • ticket.status.changed │
          │  • document.uploaded     │
          └─────────────────────────┘

Infrastructure complémentaire :
  • Eureka     :8761  — Service Discovery
  • Config Srv :8888  — Configuration centralisée
  • Keycloak   :8180  — IAM (JWT, rôles, scopes)
  • PostgreSQL  :5432  — Base de données
  • MinIO      :9000  — Stockage S3 fichiers
  • Zipkin     :9411  — Tracing distribué
  • Prometheus :9090  — Métriques
```

---

## Prérequis

| Outil        | Version minimale |
|--------------|-----------------|
| Docker       | 24+             |
| Docker Compose | 2.20+         |
| Java JDK     | 17+             |
| Maven        | 3.9+            |

---

## Démarrage rapide

### 1. Cloner et builder

```bash
git clone https://github.com/boaz-study/ticketflow.git
cd ticketflow

# Builder tous les modules Maven
mvn clean package -DskipTests
```

### 2. Lancer toute l'infrastructure en une commande

```bash
docker-compose up -d
```

Les services démarrent dans l'ordre suivant (géré par `depends_on`) :
1. PostgreSQL + Zookeeper + Kafka + MinIO
2. Keycloak (dépend de PostgreSQL)
3. Eureka Server
4. Config Server (dépend d'Eureka)
5. user-service, ticket-service, notification-service, document-service
6. API Gateway

### 3. Vérifier le démarrage

```bash
# Statut de tous les conteneurs
docker-compose ps

# Logs d'un service spécifique
docker-compose logs -f user-service

# Voir les services enregistrés dans Eureka
open http://localhost:8761
```

### 4. Obtenir un token JWT

```bash
# Token ADMIN
curl -X POST http://localhost:8180/realms/ticketflow/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=ticketflow-api" \
  -d "client_secret=ticketflow-secret" \
  -d "username=admin" \
  -d "password=admin123"

# Token AGENT
curl -X POST http://localhost:8180/realms/ticketflow/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=ticketflow-api" \
  -d "client_secret=ticketflow-secret" \
  -d "username=agent01" \
  -d "password=agent123"

# Token USER
curl -X POST http://localhost:8180/realms/ticketflow/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=ticketflow-api" \
  -d "client_secret=ticketflow-secret" \
  -d "username=user01" \
  -d "password=user123"
```

Décoder le token sur https://jwt.io pour voir les rôles et permissions.

---

## Modèle de sécurité RBAC+ABAC

### Concept hybride

```
RBAC : Rôle → contient des Permissions
ABAC : Endpoint → protégé par Permission (scope), PAS par Rôle

Exemple :
  ADMIN rôle   → [user:create, user:read, ticket:create, ...]
  AGENT rôle   → [ticket:create, ticket:read, ticket:update, ...]
  USER rôle    → [ticket:create, ticket:read, ...]

  POST /api/users  → @PreAuthorize("hasAuthority('user:create')")
                       ✅ ADMIN (a user:create)
                       ❌ AGENT (n'a pas user:create)
                       ❌ USER  (n'a pas user:create)
```

### Rôles prédéfinis

| Rôle  | Permissions |
|-------|-------------|
| ADMIN | user:create, user:read, user:update, user:delete, user:manage-roles, ticket:create, ticket:read, ticket:update, ticket:delete, ticket:comment, document:upload, document:read, document:download, notification:read, notification:send |
| AGENT | ticket:create, ticket:read, ticket:update, ticket:comment, document:upload, document:read, document:download, notification:read |
| USER  | ticket:create, ticket:read, ticket:comment, document:upload, document:read, document:download |

### Synchronisation Keycloak

Quand vous attribuez un rôle via l'API :
```
PUT /api/users/{id}/roles  →  BDD locale mise à jour
                            →  Keycloak mis à jour (rôles + attribut permissions)
                            →  Prochain login : JWT contient les nouvelles permissions
```

### Format JWT émis par Keycloak

```json
{
  "sub": "user-uuid",
  "preferred_username": "agent01",
  "roles": ["AGENT"],
  "authorities": ["ticket:create", "ticket:read", "ticket:update", "ticket:comment",
                  "document:upload", "document:read", "document:download", "notification:read"],
  "scope": "openid profile"
}
```

---

## Endpoints par service

### user-service (port 8081)

| Méthode | Endpoint                      | Scope requis        | Description |
|---------|-------------------------------|---------------------|-------------|
| POST    | /api/users                    | user:create         | Créer un utilisateur + Keycloak + Kafka |
| GET     | /api/users/{id}               | user:read           | Profil avec rôles et permissions |
| PUT     | /api/users/{id}/roles         | user:manage-roles   | Attribuer rôles + sync Keycloak |
| POST    | /api/roles                    | user:manage-roles   | Créer un rôle |
| GET     | /api/roles                    | user:read           | Lister les rôles |
| PUT     | /api/roles/{id}/permissions   | user:manage-roles   | Associer permissions à un rôle |
| GET     | /api/permissions              | user:read           | Lister les permissions |

### ticket-service (port 8082)

| Méthode | Endpoint                      | Scope requis  | Description |
|---------|-------------------------------|---------------|-------------|
| POST    | /api/tickets                  | ticket:create | Créer (Feign user-service + Kafka) |
| GET     | /api/tickets                  | ticket:read   | Liste paginée |
| GET     | /api/tickets/{id}             | ticket:read   | Détail complet |
| PATCH   | /api/tickets/{id}/status      | ticket:update | Changer statut + Kafka |
| POST    | /api/tickets/{id}/comments    | ticket:comment| Ajouter commentaire |

### notification-service (port 8083)

| Méthode | Endpoint                      | Scope requis        | Description |
|---------|-------------------------------|---------------------|-------------|
| GET     | /api/notifications/history    | notification:read   | Historique |
| POST    | /api/notifications/send       | notification:send   | Envoi manuel |

### document-service (port 8084)

| Méthode | Endpoint                        | Scope requis       | Description |
|---------|---------------------------------|--------------------|-------------|
| POST    | /api/documents/upload           | document:upload    | Upload MinIO + Kafka |
| GET     | /api/documents/{id}             | document:read      | Métadonnées |
| GET     | /api/documents/{id}/download    | document:download  | URL présignée 5 min |

---

## Tester avec Postman / curl

### Créer un utilisateur

```bash
TOKEN=$(curl -s -X POST http://localhost:8180/realms/ticketflow/protocol/openid-connect/token \
  -d "grant_type=password&client_id=ticketflow-api&client_secret=ticketflow-secret&username=admin&password=admin123" \
  | jq -r '.access_token')

curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jdupont",
    "email": "jdupont@example.com",
    "firstName": "Jean",
    "lastName": "Dupont",
    "password": "dupont123"
  }'
```

### Créer un ticket

```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Problème de connexion VPN",
    "description": "Impossible de me connecter depuis hier soir",
    "priority": "HIGH"
  }'
```

### Changer le statut d'un ticket

```bash
curl -X PATCH http://localhost:8080/api/tickets/{TICKET_ID}/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "IN_PROGRESS"}'
```

### Uploader un document

```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/document.pdf" \
  -F "ticketId={TICKET_ID}"
```

### Vérifier le rejet sans JWT (doit retourner 401)

```bash
curl -X GET http://localhost:8080/api/users
# → HTTP 401 Unauthorized
```

### Vérifier le rejet avec mauvais scope (doit retourner 403)

```bash
# Token USER qui n'a pas user:create
USER_TOKEN=$(curl -s -X POST http://localhost:8180/realms/ticketflow/protocol/openid-connect/token \
  -d "grant_type=password&client_id=ticketflow-api&client_secret=ticketflow-secret&username=user01&password=user123" \
  | jq -r '.access_token')

curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@test.com","firstName":"Test","lastName":"User"}'
# → HTTP 403 Forbidden
```

---

## URLs de référence

| Service              | URL                                      |
|----------------------|------------------------------------------|
| API Gateway          | http://localhost:8080                    |
| Swagger Aggregateur  | http://localhost:8080/swagger-ui.html    |
| Eureka Dashboard     | http://localhost:8761                    |
| Config Server        | http://localhost:8888                    |
| Keycloak Admin       | http://localhost:8180/admin (admin/admin)|
| Zipkin UI            | http://localhost:9411                    |
| Prometheus           | http://localhost:9090                    |
| MinIO Console        | http://localhost:9001 (minioadmin/minioadmin)|
| Kafka UI             | http://localhost:8090                    |
| JWT Decoder          | https://jwt.io                           |

---

## Architecture technique détaillée

### Communication synchrone (FeignClient + Resilience4j)

```
ticket-service  ──Feign──►  user-service
                              │
                    CircuitBreaker (ouvre après 50% échecs sur 10 appels)
                    Retry (3 tentatives, backoff exponentiel 500ms)
                    Fallback (réponse dégradée si circuit OPEN)
```

### Communication asynchrone (Kafka)

```
user-service   ──publish──►  user.created        ──consume──►  notification-service
ticket-service ──publish──►  ticket.created      ──consume──►  notification-service
ticket-service ──publish──►  ticket.status.changed ──consume──► notification-service
document-service ──publish──► document.uploaded  ──consume──►  ticket-service
```

### Propagation JWT inter-services

```
Client → [JWT] → API Gateway → [valide JWT] → ticket-service
                                                │
                                         FeignJwtInterceptor
                                                │
                                    [même JWT propagé] → user-service
```

### Tracing distribué

```
Requête POST /api/tickets
  → API Gateway      [traceId: abc123, spanId: 001]
  → ticket-service   [traceId: abc123, spanId: 002]
  → user-service     [traceId: abc123, spanId: 003]

Même traceId dans les logs de TOUS les services → Zipkin agrège
```

### Structure du projet

```
ticketflow/
├── docker-compose.yml        # Infrastructure complète
├── prometheus.yml            # Configuration scraping
├── init-db.sql               # Initialisation schémas PostgreSQL
├── keycloak/
│   └── realm-ticketflow.json # Realm Keycloak (rôles, users, scopes)
├── config-repo/              # Configurations Git centralisées
│   └── application.yml       # Config commune à tous les services
├── eureka-server/            # Service Discovery (port 8761)
├── config-server/            # Config centralisée (port 8888)
├── api-gateway/              # Point d'entrée, JWT filter (port 8080)
├── user-service/             # Utilisateurs, RBAC, Keycloak sync (port 8081)
├── ticket-service/           # Tickets, FeignClient, Resilience4j (port 8082)
├── notification-service/     # Kafka consumers, emails mock (port 8083)
├── document-service/         # MinIO upload/download (port 8084)
└── README.md
```

---

## Technologies utilisées

| Technologie            | Version  | Rôle |
|------------------------|----------|------|
| Spring Boot            | 3.5.13   | Framework principal |
| Spring Cloud Gateway   | 2025.0.0 | API Gateway reactive |
| Spring Security OAuth2 | 3.5.0    | Validation JWT ABAC |
| Keycloak               | 26.0     | IAM, JWT, RBAC |
| Spring Cloud Netflix Eureka | 2025.0.0 | Service Discovery |
| Spring Cloud Config    | 2025.0.0 | Configuration centralisée |
| OpenFeign              | 2025.0.0 | Communication sync déclarative |
| Resilience4j           | 2.1.0    | CircuitBreaker + Retry + Fallback |
| Apache Kafka           | 3.5.0    | Message broker async |
| MinIO                  | 8.5.7    | Stockage S3 compatible |
| Spring Micrometer + Zipkin | 3.2.0    | Tracing distribué |
| Prometheus             | latest   | Métriques |
| Springdoc OpenAPI      | 2.3.0    | Swagger UI |
| PostgreSQL             | 15       | Base de données |
| Docker Compose         | --       | Orchestration locale |

---

