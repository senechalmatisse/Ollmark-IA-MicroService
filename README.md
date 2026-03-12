# MS-OLLMARK-IA

Microservice d'intelligence artificielle pour l'assistant de design **Ollmark**, intégré à la plateforme [Penpot](https://penpot.app). Ce service expose une API de chat conversationnel avec streaming, orchestre des modèles de langage locaux via **Ollama**, et exécute des actions de design directement dans Penpot via un plugin WebSocket.

---

## Table des matières

- [Présentation](#présentation)
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Structure du projet](#structure-du-projet)
- [Fonctionnalités](#fonctionnalités)
- [Prérequis](#prérequis)
- [Installation et lancement](#installation-et-lancement)
- [Configuration](#configuration)
- [API REST](#api-rest)
- [WebSocket Plugin](#websocket-plugin)
- [Tests](#tests)
- [Qualité du code](#qualité-du-code)

---

## Présentation

`ms-ollmark-ia` est le cerveau de l'assistant Ollmark. Il reçoit les messages des utilisateurs, analyse leur intention, sélectionne les outils Penpot appropriés et retourne des réponses en streaming (Server-Sent Events). Le service peut :

- **Créer et modifier des éléments graphiques** dans Penpot (formes, textes, tableaux, logos...) en générant et exécutant du JavaScript dans le plugin
- **Inspecter** la page courante du projet pour comprendre son contexte
- **Rechercher des templates** marketing via RAG (Retrieval-Augmented Generation) et les appliquer
- **Mémoriser** l'historique conversationnel par session, stocké en base de données PostgreSQL

---

## Architecture

Le projet suit une **architecture hexagonale (Ports & Adapters)** stricte :

```
┌─────────────────────────────────────────────────────────────────┐
│                        Adaptateurs d'ENTRÉE                     │
│          AiController (REST/SSE)  │  PluginWebSocketHandler     │
└─────────────────────┬─────────────────────────┬─────────────────┘
                      │                         │
┌─────────────────────▼─────────────────────────▼─────────────────┐
│                    Ports (interfaces)                           │
│  ConversationChatUseCase  │  ExecuteCodeUseCase                 │
└─────────────────────┬───────────────────────────────────────────┘
                      │
┌─────────────────────▼─────────────────────────────────────────  ┐
│                    Couche Application                           │
│  Use Cases │ IntentRouter │ Tools Penpot │ Pipelines │ Advisors │
└─────────────────────┬───────────────────────────────────────────┘
                      │
┌─────────────────────▼─────────────────────────────────────────  ┐
│                    Adaptateurs de SORTIE                        │
│       OllamaAiAdapter  │  PluginBridgeAdapter                  │
└─────────────────────┬───────────────────────────────┬───────────┘
                      │                               │
                 Ollama (LLMs)              Plugin Penpot (WS)
```

### Pipeline de traitement IA (4 étapes)

```
Requête utilisateur
      │
      ▼
[1] RequestComplexityAnalyzer ──► SIMPLE / CREATIVE / COMPLEX
      │
      ▼
[2] IntentRouterService (llama3.1) ──► Set<ToolCategory>
      │
      ▼
[3] ToolCategoryResolver ──► instances de tools filtrées
      │
      ▼
[4] ChatClientFactory (qwen3:8b) ──► réponse en streaming SSE
```

---

## Technologies

| Composant | Technologie | Version |
|---|---|---|
| Framework | Spring Boot | 3.5.8 |
| Langage | Java | 21 |
| IA / LLM | Spring AI + Ollama | 1.1.2 |
| Modèle d'exécution | `qwen3:8b` | — |
| Modèle de routing | `llama3.1` | — |
| Modèle d'embeddings | `embeddinggemma-300m` (GGUF Q8) | — |
| Streaming | Spring WebFlux / Project Reactor | — |
| WebSocket | Spring WebSocket | — |
| Mémoire conversationnelle | Spring AI JDBC Chat Memory | — |
| RAG | Spring AI VectorStore (in-memory) | — |
| Base de données | PostgreSQL | — |
| Migrations | Flyway | — |
| ORM | Spring Data JPA / Hibernate | — |
| Cache | Caffeine | — |
| Documentation API | SpringDoc OpenAPI | 2.5.0 |
| Conteneurisation | Docker (Alpine JRE 21) | — |

---

## Structure du projet

```
ms-ollmark-ia/
├── pom.xml
├── Dockerfile
├── bruno/                              # Collection de tests API (Bruno)
└── src/
    ├── main/
    │   ├── java/com/penpot/ai/
    │   │   ├── PenpotAiApplication.java
    │   │   ├── adapters/
    │   │   │   ├── in/                 # Contrôleurs REST et WebSocket
    │   │   │   └── out/                # Adaptateurs Ollama et Plugin
    │   │   ├── application/
    │   │   │   ├── advisor/            # Advisors Spring AI (injection de contexte)
    │   │   │   ├── persistance/        # Entités JPA et repositories
    │   │   │   ├── router/             # Routeur d'intention (IntentRouterService)
    │   │   │   ├── service/            # Services métier (RAG, cache, orchestration)
    │   │   │   ├── tools/              # Tools Penpot (function calling)
    │   │   │   └── usecases/           # Implémentations des use cases
    │   │   ├── core/
    │   │   │   ├── domain/             # Entités métier, enums, commandes
    │   │   │   └── ports/              # Interfaces (ports d'entrée et de sortie)
    │   │   ├── infrastructure/         # Configuration Spring, sessions, formatters
    │   │   ├── model/                  # DTOs (PluginTaskRequest/Response)
    │   │   └── shared/                 # Exceptions et utilitaires partagés
    │   └── resources/
    │       ├── application.yml
    │       ├── db/migration/           # Scripts Flyway (V1, V3)
    │       ├── data/rag/templates/     # 18 templates JSON marketing (RAG)
    │       └── js/tools/               # Scripts JavaScript exécutés dans Penpot
    │           ├── inspector/          # Lecture du contexte de page
    │           ├── asset/              # Gestion des assets
    │           ├── content/            # Contenu riche (titres, images, logos)
    │           ├── engine/             # Moteurs de rendu thématique
    │           ├── logo/               # Génération de logos
    │           ├── a4engine/           # Format A4
    │           └── pipeline/           # Pipeline de sections
    └── test/
        └── java/com/penpot/ai/         # Tests unitaires et d'intégration
```

---

## Fonctionnalités

### Tools Penpot (Function Calling)

L'IA dispose d'un ensemble de tools qu'elle peut invoquer automatiquement :

| Catégorie | Exemples de fonctions |
|---|---|
| **Création de formes** | `createRectangle`, `createEllipse`, `createText`, `createBoard`, `createStar`, `createBoolean` |
| **Inspection** | `getPageContext`, `getPropertiesFromShape`, `getChildrenFromShape`, `getShapesColors` |
| **Mise en page** | Alignement, distribution, groupement |
| **Transformation** | Déplacement, redimensionnement, rotation |
| **Contenu** | Titres, paragraphes, images, logos, sections A4 |
| **Assets** | Gestion des composants et polices |
| **Suppression** | Suppression d'éléments |
| **Templates** | `searchTemplates`, `getTemplateDesignSpecs`, `listTemplateTypes`, `getTemplatesByTag` |

### RAG — Templates Marketing

18 templates JSON indexés couvrant **6 thèmes alimentaires** (mer, légumes, viande, boulangerie, fruits, fromage) × **3 formats** (poster A4, post/story réseaux sociaux, flyer A5). La recherche est effectuée par similarité vectorielle (seuil `0.5`, top-k `5`).

### Advisors Spring AI

| Advisor | Rôle |
|---|---|
| `InspectionFirstAdvisor` | Injecte automatiquement le contexte de page Penpot dans le prompt |
| `MissingInformationAdvisor` | Force une réponse générique si les informations sont insuffisantes |
| `ReReadingAdvisor` | Stratégie RE2 : améliore la compréhension des requêtes complexes |

### Gestion des sessions WebSocket

Deux stratégies configurables via `application.yml` :
- **Mono-utilisateur** (`SingleUserSessionStrategy`) : une seule connexion active
- **Multi-utilisateur** (`MultiUserSessionStrategy`) : plusieurs connexions simultanées

---

## Prérequis

- **Java 21**
- **Maven 3.9+** (ou utiliser le wrapper `./mvnw`)
- **Ollama** installé et accessible (avec les modèles `qwen3:8b`, `llama3.1`, `embeddinggemma-300m`)
- **PostgreSQL** (pour la mémoire conversationnelle et les entités JPA)
- **Plugin Penpot** Ollmark installé dans le navigateur (pour la communication WebSocket)

---

## Installation et lancement

### 1. Cloner le dépôt

```bash
git clone https://gitlab-dpt-info-sciences.univ-rouen.fr/m2gil/ollmark/ollmark-microservices/ollmark-backend/ms-ollmark-ia.git
cd ms-ollmark-ia
```

### 2. Configurer les variables d'environnement

```bash
export OLLAMA_BASE_URL=http://localhost:11434
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ollmark
export SPRING_DATASOURCE_USERNAME=ollmark
export SPRING_DATASOURCE_PASSWORD=secret
```

### 3. Lancer avec Maven

```bash
./mvnw spring-boot:run
```

### 4. Lancer avec Docker

```bash
docker build -t ms-ollmark-ia .
docker run -p 8080:8080 -p 8081:8081 \
  -e OLLAMA_BASE_URL=http://host.docker.internal:11434 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/ollmark \
  -e SPRING_DATASOURCE_USERNAME=ollmark \
  -e SPRING_DATASOURCE_PASSWORD=secret \
  ms-ollmark-ia
```

---

## Configuration

Le fichier `src/main/resources/application.yml` contient tous les paramètres :

| Section | Paramètre | Valeur par défaut |
|---|---|---|
| Serveur HTTP | `server.port` | `8080` |
| WebSocket Plugin | `penpot.websocket.port` | `8081` |
| Modèle routeur | `spring.ai.ollama.chat.options.model` (router) | `llama3.1` |
| Modèle exécuteur | `spring.ai.ollama.chat.options.model` (executor) | `qwen3:8b` |
| Modèle embeddings | `spring.ai.ollama.embedding.options.model` | `embeddinggemma-300m` |
| Mémoire chat | Max messages | `50` par conversation |
| RAG | Seuil de similarité | `0.5` |
| RAG | Top-k résultats | `5` |
| Mode multi-utilisateur | `penpot.session.multi-user` | `false` |

---

## API REST

### Swagger UI

Une fois le service lancé, la documentation interactive est disponible à :

```
http://localhost:8080/swagger-ui.html
```

### Endpoints

#### `POST /ai/chat/new` — Nouvelle conversation

Crée une nouvelle conversation et retourne son identifiant.

**Corps de la requête :**
```json
{
  "userId": "user-123"
}
```

**Réponse :**
```json
{
  "conversationId": "conv-abc-456"
}
```

---

#### `POST /ai/chat` — Chat avec streaming SSE

Envoie un message et reçoit la réponse en streaming (`text/event-stream`).

**Corps de la requête :**
```json
{
  "conversationId": "conv-abc-456",
  "message": "Crée un rectangle rouge au centre de la page",
  "userToken": "token-xyz"
}
```

**Réponse :** flux SSE de tokens textuels

---

#### `DELETE /ai/chat/{conversationId}` — Effacer l'historique

Supprime l'historique d'une conversation.

**Paramètre de chemin :** `conversationId`

---

## WebSocket Plugin

Le plugin Penpot se connecte au service via WebSocket sur le port `8081` :

```
ws://localhost:8081/plugin-ws?userToken=<token>
```

Le service envoie des tâches JavaScript au plugin (création de formes, lecture de contexte...), et le plugin retourne les résultats. La communication est gérée par `PluginWebSocketHandler` et `PluginBridgeAdapter`.

---

## Tests

```bash
# Lancer tous les tests
./mvnw test

# Lancer avec rapport de couverture JaCoCo
./mvnw verify
```

Les tests utilisent :
- **H2** comme base de données embarquée
- **WireMock** pour mocker les appels HTTP externes
- **Reactor Test** pour tester les flux réactifs

Le rapport de couverture est généré dans `target/site/jacoco/`. La couverture minimale requise est de **70%**.

---

## Qualité du code

| Outil | Configuration |
|---|---|
| **Checkstyle** | Google Java Style Guide |
| **JaCoCo** | Couverture minimale 70% |
| **SonarQube** | Analyse statique complète |

```bash
# Vérification du style
./mvnw checkstyle:check

# Analyse SonarQube (nécessite un serveur Sonar configuré)
./mvnw sonar:sonar
```
