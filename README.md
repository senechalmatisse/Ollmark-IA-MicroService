# penpot-ai-server

Microservice Spring Boot fournissant une couche d'intelligence artificielle au-dessus de [Penpot](https://penpot.app).  
Il expose une API REST et un canal WebSocket permettant à un plugin Penpot de converser avec un LLM (Ollama/Cloud) afin de créer, modifier et inspecter des éléments graphiques directement dans le designer.

---

## Sommaire

1. [Vue d'ensemble de l'architecture](#vue-densemble-de-larchitecture)
2. [Prérequis](#prérequis)
3. [Guide utilisateur — démarrage rapide](#guide-utilisateur--démarrage-rapide)
   - [3.1 Cloner le dépôt](#31-cloner-le-dépôt)
   - [3.2 Configurer les variables d'environnement](#32-configurer-les-variables-denvironnement)
   - [3.3 Démarrer la base de données (Docker)](#33-démarrer-la-base-de-données-docker)
   - [3.4 Lancer l'application](#34-lancer-lapplication)
   - [3.5 Vérifier le démarrage](#35-vérifier-le-démarrage)
   - [3.6 Accéder à Swagger UI](#36-accéder-à-swagger-ui)
4. [Profils de configuration](#profils-de-configuration)
5. [Variables d'environnement de référence](#variables-denvironnement-de-référence)
6. [API REST — référence rapide](#api-rest--référence-rapide)
7. [Canal WebSocket plugin](#canal-websocket-plugin)
8. [Architecture interne](#architecture-interne)
   - [Pipeline de traitement IA](#pipeline-de-traitement-ia)
   - [Système RAG](#système-rag)
   - [Outils Penpot disponibles](#outils-penpot-disponibles)
9. [Base de données](#base-de-données)
10. [Logs](#logs)
11. [Pistes d'amélioration suggérées](#pistes-damélioration-suggérées)

---

## Vue d'ensemble de l'architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Plugin Penpot (Angular)                  │
│               WebSocket ws://<HOST>:<PORT>/plugin               │
└───────────────────────────────┬─────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    penpot-ai-server (port 8080)                 │
│                                                                 │
│  REST /api/ai/chat ──► ConversationChatUseCase                  │
│                               │                                 │
│         ┌─────────────────────▼──────────────────┐              │
│         │           OllamaAiAdapter              │              │
│         │  1. RequestComplexityAnalyzer          │              │
│         │  2. IntentRouterService                │              │
│         │  3. PenpotToolRegistry                 │              │
│         │  4. ChatClientFactory                  │              │
│         └────────────────────┬───────────────────┘              │
│                              │ Tool calls                       │
│         ┌────────────────────▼──────────────────┐               │
│         │      PenpotToolExecutor               │               │
│         │  → PluginBridgeAdapter (WebSocket)    │               │
│         └───────────────────────────────────────┘               │
│                                                                 │
│  PostgreSQL  ◄──── Spring AI Chat Memory + Conversations        │
│  Ollama      ◄──── Embeddings + LLM                             │
└─────────────────────────────────────────────────────────────────┘
```

---

## Prérequis

| Composant | Version minimale | Notes |
|-----------|-----------------|-------|
| Java | 21 | LTS recommandé |
| Maven | 3.9+ | Wrapper `./mvnw` inclus |
| Docker & Docker Compose | 24+ | Pour PostgreSQL <i>(en local)</i> |
| Ollama | 0.3+ | Accessible depuis le serveur |
| Modèles Ollama | — | Voir ci-dessous |
| PostgreSQL | 15+ | Via Docker ou instance existante |

### Modèles Ollama requis

```bash
# Modèle exécuteur principal (génération de design)
ollama pull xxx

# Modèle routeur d'intention (classification)
ollama pull xxx

# Modèle d'embedding (RAG)
ollama pull xxx
```

---

## Guide utilisateur — démarrage rapide

### 3.1 Cloner le dépôt

```bash
git clone <url-du-dépôt>
cd penpot-ai-server
```

### 3.2 Configurer les variables d'environnement

Copiez le fichier d'exemple et renseignez les valeurs :

```bash
cp .env.example .env
```

Contenu minimal du `.env` pour un démarrage en profil `local` :

```env
# Profil actif
SPRING_PROFILES_ACTIVE=local

# Base de données
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/penpot_ai
SPRING_DATASOURCE_USERNAME=penpot_ai
SPRING_DATASOURCE_PASSWORD=penpot_ai_secret

# Variables pour le profil local (DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD)
DB_HOST=localhost
DB_PORT=5433
DB_NAME=penpot_ai
DB_USER=penpot_ai
DB_PASSWORD=penpot_ai_secret

# Ollama
OLLAMA_BASE_URL=http://localhost:11434/

# Penpot plugin
PENPOT_SERVER_ADDRESS=localhost
PENPOT_WEBSOCKET_PORT=8080

# Sécurité Swagger
SWAGGER_PASSWORD=changeme

# Clé de chiffrement Jasypt (32+ caractères hex)
CRYPTO_MASTER_KEY=5d8b57a473a25517d5b30e8675b66a050b41238072374b3d810e6598dab5f194
```

> **Important :** ne committez jamais le fichier `.env` ; il est listé dans `.gitignore`.

### 3.3 Démarrer la base de données (Docker)

```bash
docker compose up -d postgres
```

Exemple de `docker-compose.yml` minimal si non fourni dans le projet :

```yaml
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: penpot_ai
      POSTGRES_USER: penpot_ai
      POSTGRES_PASSWORD: penpot_ai_secret
    ports:
      - "5433:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

volumes:
  pgdata:
```

Flyway applique automatiquement les migrations au démarrage de l'application.  
Le schéma principal comprend : `projects`, `conversations`, `messages`, `ai_model_config`, `spring_ai_chat_memory`.

### 3.4 Lancer l'application

**Avec Maven Wrapper (recommandé) :**

```bash
./mvnw spring-boot:run
```

**Avec les variables d'environnement chargées depuis `.env` :**

```bash
export $(grep -v '^#' .env | xargs) && ./mvnw spring-boot:run
```

**Avec un profil spécifique :**

```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

**Packager et exécuter le JAR :**

```bash
./mvnw clean package -DskipTests
java -jar target/penpot-ai-server-*.jar
```

### 3.5 Vérifier le démarrage

```bash
# Health check
curl http://localhost:8080/actuator/health

# Réponse attendue
{"status":"UP"}
```

Les logs de démarrage affichent :

```
========================================
  Démarrage du serveur Penpot Serveur
========================================
...
Le serveur Penpot a démarré correctement
========================================
```

### 3.6 Accéder à Swagger UI

```
http://localhost:8080/swagger-ui.html
```

Authentification : utilisateur `admin_audit`, mot de passe = valeur de `SWAGGER_PASSWORD`.

---

## Profils de configuration

| Profil | Usage | BDD | Logs | SQL visible |
|--------|-------|-----|------|------------|
| `local` | Dev sur machine locale | `localhost:5433` | DEBUG/TRACE | Oui |
| `dev` | Dev sur serveur partagé | Variable `SPRING_DATASOURCE_URL` | DEBUG/TRACE | Oui |
| `preprod` | Recette / staging | Variable | INFO | Non |
| `prod` | Production | Variable | WARN | Non |

Sélection du profil via la variable `SPRING_PROFILES_ACTIVE`.

---

## Variables d'environnement de référence

### Variables obligatoires

| Variable | Description | Défaut |
|----------|-------------|--------|
| `SPRING_DATASOURCE_URL` | URL JDBC PostgreSQL | — |
| `SPRING_DATASOURCE_USERNAME` | Utilisateur BDD | — |
| `SPRING_DATASOURCE_PASSWORD` | Mot de passe BDD | — |
| `CRYPTO_MASTER_KEY` | Clé Jasypt pour chiffrement des secrets en BDD | — |
| `SWAGGER_PASSWORD` | Mot de passe Swagger UI | — |

### Variables Ollama

| Variable | Description | Défaut |
|----------|-------------|--------|
| `OLLAMA_BASE_URL` | URL de l'instance Ollama | `http://10.130.163.62:11434/` |
| `OLLAMA_EMBEDDING_MODEL` | Modèle d'embedding | `hf.co/unsloth/embeddinggemma-300m-GGUF:Q8_0` |
| `PENPOT_EXECUTOR_MODEL` | Modèle LLM principal | `qwen3.5:9b` |
| `PENPOT_EXECUTOR_TEMPERATURE` | Température de génération | `0.7` |
| `PENPOT_ROUTER_MODEL` | Modèle routeur d'intention | `llama3.1` |

### Variables Penpot / WebSocket

| Variable | Description | Défaut |
|----------|-------------|--------|
| `PENPOT_SERVER_ADDRESS` | Adresse IP du serveur Penpot | `localhost` |
| `PENPOT_WEBSOCKET_PORT` | Port WebSocket du plugin | `8080` |

### Variables RAG

| Variable | Description | Défaut |
|----------|-------------|--------|
| `PENPOT_RAG_SIMILARITY_THRESHOLD` | Seuil de similarité cosinus | `0.5` |
| `PENPOT_RAG_TOP_K` | Nombre de templates retournés | `3` |
| `PENPOT_RAG_QUERY_VARIANTS` | Nombre de variantes de requête | `2` |
| `PENPOT_CHAT_MEMORY_MAX_MESSAGES` | Fenêtre mémoire conversationnelle | `20` |

---

## API REST — référence rapide

### Chat IA

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `POST` | `/api/ai/chat` | Envoyer un message à l'IA |
| `GET` | `/api/ai/chat/{projectId}/history` | Récupérer l'historique |
| `POST` | `/api/ai/chat/new` | Démarrer une nouvelle conversation |
| `DELETE` | `/api/ai/chat/{projectId}` | Effacer l'historique d'une conversation |

**Exemple de requête chat :**

```bash
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "550e8400-e29b-41d4-a716-446655440000",
    "sessionId": "session-abc123",
    "message": "Crée un rectangle bleu de 400x200 pixels"
  }'
```

### Configuration IA

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/ai/config?projectId=...` | Lire la config IA du projet |
| `POST` | `/api/ai/config?projectId=...` | Mettre à jour la config IA |
| `GET` | `/api/ai/config/prompt?projectId=...` | Lire le prompt système |
| `POST` | `/api/ai/config/prompt?projectId=...` | Mettre à jour le prompt système |

### Projets, Conversations, Messages

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/ai/projects/{projectId}` | Détails d'un projet |
| `DELETE` | `/api/ai/projects/{projectId}` | Supprimer un projet |
| `GET` | `/api/ai/conversations/project/{projectId}` | Conversations d'un projet |
| `DELETE` | `/api/ai/conversations/{conversationId}` | Supprimer une conversation |
| `GET` | `/api/ai/messages/conversation/{conversationId}` | Messages d'une conversation |
| `DELETE` | `/api/ai/messages/conversation/{conversationId}` | Vider les messages |

---

## Canal WebSocket plugin

Le plugin Penpot se connecte à :

```
ws://localhost:8080/plugin
```

### Protocole

**1. Handshake serveur → plugin (à la connexion) :**
```json
{ "type": "session-id", "sessionId": "abc123" }
```

**2. Envoi d'une tâche serveur → plugin :**
```json
{
  "id": "task-uuid",
  "task": "executeCode",
  "params": { "code": "const rect = penpot.createRectangle(); ..." }
}
```

**3. Réponse plugin → serveur :**
```json
{
  "type": "task-response",
  "response": {
    "id": "task-uuid",
    "success": true,
    "data": { "result": "shape-uuid" }
  }
}
```

---

## Architecture interne

### Pipeline de traitement IA

Chaque message utilisateur traverse un pipeline en 4 étapes avant d'atteindre le LLM :

```
userMessage
    │
    ├─ (1) RequestComplexityAnalyzer
    │       → SIMPLE | CREATIVE | COMPLEX
    │
    ├─ (2) IntentRouterService (llama3.1, temperature=0.0)
    │       → Set<ToolCategory>
    │         ex: {COLOR_AND_STYLE, INSPECTION}
    │
    ├─ (3) PenpotToolRegistry
    │       → Object[] tools (sous-ensemble filtré)
    │
    └─ (4) ChatClientFactory + OllamaAiAdapter (qwen3.5:9b)
            → Exécution avec advisors, mémoire, RAG
```

**Niveaux de complexité et options LLM associées :**

| Niveau | Température | Usage |
|--------|------------|-------|
| `SIMPLE` | 0.1, topK=3 | Opération atomique (changer couleur, déplacer) |
| `CREATIVE` | 0.8, topK=5 | Suggestions esthétiques, palette, layout |
| `COMPLEX` | 0.6, thinking activé | Création complète, orchestration multi-étapes |

### Système RAG

Le RAG (Retrieval-Augmented Generation) indexe des templates marketing JSON au démarrage :

- **24 templates** : 6 thèmes (mer, légumes, viande, boulangerie, fruits, fromage) × 4 formats (poster A4, social media post, flyer A5, social media story)
- **Pipeline RAG modulaire** : `RewriteQueryTransformer` → `MultiQueryExpander` → `VectorStoreDocumentRetriever` → `ContextualQueryAugmenter`
- **Cache d'embeddings** : Caffeine, 10 000 entrées, sans expiration

### Outils Penpot disponibles

| Classe | Catégorie | Capacités |
|--------|-----------|-----------|
| `PenpotShapeTools` | `SHAPE_CREATION` | Rectangle, ellipse, texte, board, étoile, triangle, boolean |
| `PenpotTransformTools` | `SHAPE_MODIFICATION` | Rotation, scale, déplacement, redimensionnement |
| `PenpotAssetTools` | `COLOR_AND_STYLE` | Fill, gradient, stroke, ombre, opacité, border radius, interactions |
| `PenpotLayoutTools` | `LAYOUT_AND_ALIGNMENT` | Alignement, distribution, groupement, z-order, clone |
| `PenpotContentTools` | `CONTENT_AND_TEXT` | Titre, sous-titre, paragraphe, image, bouton, logo, section |
| `PenpotInspectorTools` | `INSPECTION` | Page context, propriétés, centre, hiérarchie, couleurs |
| `PenpotDeleteTools` | `DELETION` | Suppression par sélection, par ID, formes manuelles |
| `TemplateSearchTools` | `TEMPLATE_SEARCH` | Recherche RAG, specs design, filtres par type/tag |

---

## Base de données

### Schéma principal

```
projects
  └─ conversations (project_id → projects.id)
       └─ messages (conversation_id → conversations.id)

projects
  └─ ai_model_config (project_id → projects.id)

spring_ai_chat_memory   ← géré par Spring AI
```

### Migrations Flyway

Les migrations se trouvent dans `src/main/resources/db/migration/`.  
Flyway les applique automatiquement au démarrage. Le schéma est validé (pas de `ddl-auto: create`) à partir du profil `dev`.

**Migration manuelle si nécessaire :**
```bash
./mvnw flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5433/penpot_ai \
  -Dflyway.user=penpot_ai -Dflyway.password=penpot_ai_secret
```

---

## Logs

Les logs applicatifs sont écrits dans `logs/penpot.log` (configurable via `PENPOT_LOG_DIR`).

- Rotation : 10 MB par fichier, 30 fichiers, 100 MB total
- Format : `yyyy-MM-dd HH:mm:ss.SSS [thread] LEVEL logger - message`

**Accéder aux niveaux de log dynamiquement (Actuator) :**

```bash
# Lire le niveau d'un logger
curl http://localhost:8080/actuator/loggers/com.penpot.ai

# Modifier le niveau à chaud
curl -X POST http://localhost:8080/actuator/loggers/com.penpot.ai \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'
```