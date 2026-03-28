# Urimaigal Backend

> Spring Boot 3 · MySQL · NamedParameterJdbcTemplate · Apache Artemis JMS · Elasticsearch 8 · JWT Security

Legal services platform backend for the **Urimaigal** Vue 3 frontend.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Prerequisites](#prerequisites)
3. [Step 1 — MySQL Setup](#step-1--mysql-setup)
4. [Step 2 — Apache Artemis Setup](#step-2--apache-artemis-setup)
5. [Step 3 — Elasticsearch Setup](#step-3--elasticsearch-setup)
6. [Step 4 — Configure the Application](#step-4--configure-the-application)
7. [Step 5 — Build and Run](#step-5--build-and-run)
8. [Step 6 — Connect the Vue Frontend](#step-6--connect-the-vue-frontend)
9. [API Reference](#api-reference)
10. [Project Structure](#project-structure)
11. [Design Decisions](#design-decisions)
12. [Troubleshooting](#troubleshooting)

---

## Architecture Overview

```
Vue 3 Frontend (port 5173)
        │
        ▼ HTTP/REST + JWT Bearer token
┌───────────────────────┐
│  Spring Boot 3 (8080) │
│  ┌─────────────────┐  │
│  │  Controllers    │  │
│  │  Services       │  │
│  │  Repositories   │◄─┼──► MySQL 8 (NamedParameterJdbcTemplate)
│  │  (NamedJdbc)    │  │
│  └────────┬────────┘  │
│           │           │
│  ┌────────▼────────┐  │
│  │  JmsTemplate    │◄─┼──► Apache Artemis (port 61616)
│  │  (Producer)     │  │         │
│  │  @JmsListener   │◄─┼─────────┘
│  │  (Consumer)     │  │
│  └────────┬────────┘  │
│           │           │
│  ┌────────▼────────┐  │
│  │ ElasticsearchClient│◄┼──► Elasticsearch 8 (port 9200)
│  │ (ESClient config)│  │
│  └─────────────────┘  │
└───────────────────────┘
```

**Key technology choices:**

| Technology | Usage |
|---|---|
| `NamedParameterJdbcTemplate` | All DB queries — no JPA/ORM |
| `JmsTemplate` + `@JmsListener` | Producer-consumer pattern via Artemis |
| `ElasticsearchClient` | ES Java API Client (not Spring Data ES) |
| Constructor injection | No Lombok — all DI is explicit |
| JWT (JJWT 0.11.5) | Stateless auth, no sessions |

---

## Prerequisites

Make sure the following are installed before starting:

| Tool | Version | Download |
|---|---|---|
| Java (JDK) | 17+ | https://adoptium.net |
| Maven | 3.8+ | https://maven.apache.org |
| MySQL | 8.0+ | https://dev.mysql.com/downloads/ |
| Apache Artemis | 2.31+ | https://activemq.apache.org/components/artemis/download/ |
| Elasticsearch | 8.x | https://www.elastic.co/downloads/elasticsearch |

---

## Step 1 — MySQL Setup

### 1.1 Start MySQL

```bash
# macOS (Homebrew)
brew services start mysql

# Linux (systemd)
sudo systemctl start mysql

# Windows
net start MySQL80
```

### 1.2 Create the database and user

```sql
-- Connect as root
mysql -u root -p

-- Create database
CREATE DATABASE IF NOT EXISTS urimaigal_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Create a dedicated user (recommended for production)
CREATE USER 'urimaigal'@'localhost' IDENTIFIED BY 'yourStrongPassword';
GRANT ALL PRIVILEGES ON urimaigal_db.* TO 'urimaigal'@'localhost';
FLUSH PRIVILEGES;

EXIT;
```

### 1.3 Tables and seed data

The app creates the schema and seeds lawyer data **automatically on startup** via `schema.sql` and `data.sql` in `src/main/resources/`. No manual SQL import needed.

If you want to inspect or import manually:

```bash
mysql -u root -p urimaigal_db < src/main/resources/schema.sql
mysql -u root -p urimaigal_db < src/main/resources/data.sql
```

---

## Step 2 — Apache Artemis Setup

### 2.1 Download and extract Artemis

```bash
# Download from https://activemq.apache.org/components/artemis/download/
# Example with version 2.31.2
wget https://downloads.apache.org/activemq/activemq-artemis/2.31.2/apache-artemis-2.31.2-bin.tar.gz
tar -xzf apache-artemis-2.31.2-bin.tar.gz
cd apache-artemis-2.31.2
```

### 2.2 Create a broker instance

```bash
# Create a broker named 'urimaigal-broker'
./bin/artemis create /opt/artemis/urimaigal-broker \
  --user artemis \
  --password artemis \
  --allow-anonymous \
  --no-autotune

# On Windows:
bin\artemis.cmd create C:\artemis\urimaigal-broker --user artemis --password artemis --allow-anonymous
```

### 2.3 Start the broker

```bash
# Linux/macOS
/opt/artemis/urimaigal-broker/bin/artemis run

# Windows
C:\artemis\urimaigal-broker\bin\artemis.cmd run
```

### 2.4 Verify the broker is running

Open http://localhost:8161 in your browser (default credentials: `artemis` / `artemis`).

The following queues are created automatically when the application first sends a message:

- `booking.consultation.queue` — booking events (created/cancelled)
- `notification.queue` — email notifications
- `chat.message.queue` — async chat message persistence

---

## Step 3 — Elasticsearch Setup

### 3.1 Download and start Elasticsearch

```bash
# macOS (Homebrew)
brew install elasticsearch
brew services start elasticsearch

# Linux
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-8.12.2-linux-x86_64.tar.gz
tar -xzf elasticsearch-8.12.2-linux-x86_64.tar.gz
cd elasticsearch-8.12.2
./bin/elasticsearch
```

### 3.2 Disable security for local development

Elasticsearch 8 ships with security enabled. For local development, disable it:

```bash
# Edit elasticsearch.yml
nano config/elasticsearch.yml

# Add / change these lines:
xpack.security.enabled: false
xpack.security.http.ssl.enabled: false
```

Restart Elasticsearch after saving.

### 3.3 Verify Elasticsearch is running

```bash
curl http://localhost:9200
# Expected: { "name": "...", "cluster_name": "elasticsearch", ... }
```

### 3.4 Lawyer index

The app **automatically creates** the `lawyers` Elasticsearch index on startup (via `@PostConstruct` in `LawyerSearchService`). No manual index creation needed.

To manually reindex all lawyers from MySQL into ES after startup:

```bash
curl -X POST http://localhost:8080/api/lawyers/reindex \
  -H "Authorization: Bearer <your-jwt-token>"
```

---

## Step 4 — Configure the Application

### 4.1 Edit `application.properties`

Open `src/main/resources/application.properties` and update:

```properties
# MySQL — change password and user if you used a dedicated user in Step 1.2
spring.datasource.url=jdbc:mysql://localhost:3306/urimaigal_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD_HERE

# Artemis — must match what you set in Step 2.2
spring.artemis.broker-url=tcp://localhost:61616
spring.artemis.user=artemis
spring.artemis.password=artemis

# Elasticsearch — if security is enabled, set username/password
elasticsearch.host=localhost
elasticsearch.port=9200
elasticsearch.scheme=http
elasticsearch.username=
elasticsearch.password=

# JWT — change this to a long, random secret in production
app.jwt.secret=urimaigal-super-secret-jwt-key-2024-legal-platform-secure-key
app.jwt.expiration-ms=86400000

# CORS — must include your Vue dev server origin
app.cors.allowed-origins=http://localhost:5173,http://localhost:3000
```

---

## Step 5 — Build and Run

### 5.1 Build the project

```bash
cd urimaigal-backend

# Download dependencies and compile
mvn clean package -DskipTests
```

### 5.2 Run the application

```bash
# Option A — Maven
mvn spring-boot:run

# Option B — JAR
java -jar target/urimaigal-backend-1.0.0.jar
```

### 5.3 Verify startup

You should see logs like:

```
INFO  com.urimaigal.UrimaigalApplication       - Started UrimaigalApplication in 4.2 seconds
INFO  com.urimaigal.search.LawyerSearchService - Elasticsearch index 'lawyers' created
INFO  com.urimaigal.config.JmsConfig           - Artemis JMS ConnectionFactory configured: tcp://localhost:61616
```

Test with:

```bash
# Health check
curl http://localhost:8080/api/lawyers

# Should return all 8 seeded lawyers
```

---

## Step 6 — Connect the Vue Frontend

### 6.1 Update the frontend stores

In each Pinia store, replace the simulated `setTimeout` calls with real `fetch` calls to the backend.

**Example — `src/stores/auth.ts`:**

```typescript
const BASE_URL = 'http://localhost:8080/api'

async function login(email: string, password: string): Promise<boolean> {
  isLoading.value = true
  try {
    const res = await fetch(`${BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    })
    const data = await res.json()
    if (!res.ok) throw new Error(data.message)

    // Store JWT
    localStorage.setItem('token', data.data.token)
    user.value = {
      id: data.data.userId,
      name: data.data.name,
      email: data.data.email,
      consultations: []
    }
    return true
  } catch (e: any) {
    error.value = e.message
    return false
  } finally {
    isLoading.value = false
  }
}
```

**Helper — authenticated fetch:**

```typescript
// src/utils/api.ts
export const BASE_URL = 'http://localhost:8080/api'

export async function authFetch(path: string, options: RequestInit = {}) {
  const token = localStorage.getItem('token')
  return fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    }
  })
}
```

**Example — `src/stores/lawyers.ts` search:**

```typescript
async function searchLawyers(query: string) {
  const res = await fetch(`${BASE_URL}/api/lawyers/search?q=${encodeURIComponent(query)}`)
  const data = await res.json()
  lawyers.value = data.data
}
```

**Example — `src/stores/booking.ts`:**

```typescript
async function bookConsultation(lawyerId, lawyerName, date, time, mode, fee) {
  const res = await authFetch('/bookings', {
    method: 'POST',
    body: JSON.stringify({ lawyerId, date, time, mode })
  })
  const data = await res.json()
  if (!res.ok) throw new Error(data.message)
  return data.data
}
```

**Example — `src/stores/chat.ts`:**

```typescript
async function sendMessage(content: string) {
  const res = await authFetch('/chat/message', {
    method: 'POST',
    body: JSON.stringify({ content })
  })
  const data = await res.json()
  messages.value.push(data.data)
}
```

### 6.2 Start the Vue dev server

```bash
cd urimaigal   # your frontend folder
npm install
npm run dev    # starts on http://localhost:5173
```

---

## API Reference

All responses follow this envelope:

```json
{
  "success": true,
  "message": "Success",
  "data": { ... }
}
```

### Auth (public)

| Method | Endpoint | Body | Description |
|---|---|---|---|
| POST | `/api/auth/register` | `{name, email, password, phone?}` | Register new user |
| POST | `/api/auth/login` | `{email, password}` | Login, returns JWT |

### Lawyers (public)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/lawyers` | All lawyers |
| GET | `/api/lawyers/{id}` | Single lawyer |
| GET | `/api/lawyers/available` | Only available lawyers |
| GET | `/api/lawyers/specialization/{s}` | By specialization |
| GET | `/api/lawyers/filter?category=&minRating=&maxFee=&language=&available=` | Multi-filter (MySQL) |
| GET | `/api/lawyers/search?q=` | Full-text search (Elasticsearch → MySQL fallback) |
| GET | `/api/lawyers/search/filter?specialization=&language=&available=&minRating=&maxFee=` | ES bool filter |
| POST | `/api/lawyers` | Create lawyer |
| PUT | `/api/lawyers/{id}` | Update lawyer |
| PATCH | `/api/lawyers/{id}/availability?available=true` | Toggle availability |
| DELETE | `/api/lawyers/{id}` | Delete lawyer |
| POST | `/api/lawyers/reindex` | Re-sync MySQL → Elasticsearch |

### Bookings (JWT required)

| Method | Endpoint | Body | Description |
|---|---|---|---|
| POST | `/api/bookings` | `{lawyerId, date, time, mode, notes?}` | Book consultation |
| GET | `/api/bookings` | — | Current user's bookings |
| GET | `/api/bookings/{id}` | — | Single booking |
| PATCH | `/api/bookings/{id}/cancel` | — | Cancel booking |
| PATCH | `/api/bookings/{id}/complete` | — | Mark completed |
| GET | `/api/bookings/lawyer/{lawyerId}` | — | Lawyer's bookings |

### Chat (JWT required)

| Method | Endpoint | Body | Description |
|---|---|---|---|
| POST | `/api/chat/message` | `{content}` | Send message, get bot reply |
| GET | `/api/chat/history` | — | Full chat history |
| GET | `/api/chat/history/recent` | — | Last 50 messages |
| DELETE | `/api/chat/history` | — | Clear history |

### Users (JWT required)

| Method | Endpoint | Body | Description |
|---|---|---|---|
| GET | `/api/users/me` | — | Current user profile |
| PATCH | `/api/users/me` | `{name?, phone?, avatar?}` | Update profile |

### Search (public)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/search/lawyers?q=` | ES full-text search |
| GET | `/api/search/lawyers/filter?...` | ES filtered search |

---

## Project Structure

```
urimaigal-backend/
├── pom.xml
└── src/main/
    ├── java/com/urimaigal/
    │   ├── UrimaigalApplication.java          # Entry point (@SpringBootApplication @EnableJms)
    │   │
    │   ├── config/
    │   │   ├── ElasticsearchConfig.java        # RestClient → ElasticsearchTransport → ElasticsearchClient
    │   │   ├── JmsConfig.java                  # ConnectionFactory, JmsTemplate, ListenerContainerFactory
    │   │   └── SecurityConfig.java             # JWT filter chain, CORS, PasswordEncoder
    │   │
    │   ├── security/
    │   │   ├── JwtUtil.java                    # Token generation + validation (JJWT)
    │   │   └── JwtAuthenticationFilter.java    # OncePerRequestFilter — reads Bearer token
    │   │
    │   ├── model/
    │   │   ├── Lawyer.java
    │   │   ├── User.java
    │   │   ├── Consultation.java
    │   │   └── ChatMessage.java
    │   │
    │   ├── dto/
    │   │   ├── ApiResponse.java                # Generic {success, message, data} wrapper
    │   │   ├── LoginRequest.java
    │   │   ├── RegisterRequest.java
    │   │   ├── AuthResponse.java
    │   │   ├── BookingRequest.java
    │   │   ├── BookingEvent.java               # JMS message payload (Serializable)
    │   │   ├── ChatRequest.java
    │   │   └── LawyerFilterRequest.java
    │   │
    │   ├── repository/                         # All DB access via NamedParameterJdbcTemplate
    │   │   ├── LawyerRepository.java
    │   │   ├── UserRepository.java
    │   │   ├── ConsultationRepository.java
    │   │   └── ChatMessageRepository.java
    │   │
    │   ├── messaging/                          # JMS Producer + Consumers
    │   │   ├── BookingProducer.java            # JmsTemplate.convertAndSend()
    │   │   ├── BookingConsumer.java            # @JmsListener (booking + notification queues)
    │   │   └── ChatMessageConsumer.java        # @JmsListener (chat queue)
    │   │
    │   ├── search/
    │   │   └── LawyerSearchService.java        # ElasticsearchClient — index, search, delete
    │   │
    │   ├── service/
    │   │   ├── AuthService.java
    │   │   ├── LawyerService.java
    │   │   ├── ConsultationService.java
    │   │   ├── ChatService.java
    │   │   └── UserService.java
    │   │
    │   ├── controller/
    │   │   ├── AuthController.java
    │   │   ├── LawyerController.java
    │   │   ├── ConsultationController.java
    │   │   ├── ChatController.java
    │   │   ├── UserController.java
    │   │   └── SearchController.java
    │   │
    │   └── exception/
    │       ├── ResourceNotFoundException.java
    │       ├── ConflictException.java
    │       ├── UnauthorizedException.java
    │       └── GlobalExceptionHandler.java     # @RestControllerAdvice — maps exceptions to HTTP codes
    │
    └── resources/
        ├── application.properties
        ├── schema.sql                          # Auto-run on startup (spring.sql.init.mode=always)
        └── data.sql                            # Seeds 8 lawyers on startup
```

---

## Design Decisions

### No JPA / No Lombok

- **`NamedParameterJdbcTemplate`** is used for all database operations. Named parameters (`:id`, `:name`) are safer and more readable than `?` positional parameters.
- **No Lombok** — all constructors, getters, setters are written explicitly. This makes the code verbose but fully transparent for IDE tooling, debugging, and code review.

### Constructor Injection

Every Spring bean uses constructor injection (not `@Autowired` field injection). Benefits:
- Dependencies are immutable (`final` fields).
- The class is easily testable without a Spring context.
- Missing dependencies fail fast at compile time.

### Producer-Consumer via Artemis

When a user books a consultation the HTTP request returns immediately after saving to MySQL. Two JMS messages are then sent asynchronously:

1. **`booking.consultation.queue`** — consumed by `BookingConsumer.processBookingEvent()` for post-processing (calendar updates, analytics).
2. **`notification.queue`** — consumed by `BookingConsumer.processNotification()` for email/SMS confirmation.

Chat bot replies are also persisted asynchronously via `chat.message.queue` consumed by `ChatMessageConsumer`.

### Elasticsearch Approach

`ElasticsearchClient` (the official ES Java API client) is configured manually via `ElasticsearchConfig` — **not Spring Data Elasticsearch**. This gives full control over index mappings, query DSL, and transport setup.

The search service provides:
- **Full-text multi-match** across `name`, `specialization`, `location`, `bio`, `languages` with fuzziness.
- **Bool filter queries** for structured filtering.
- **Auto-fallback** to MySQL `LIKE` queries if Elasticsearch is unavailable.
- **Auto-index creation** on startup via `@PostConstruct`.

---

## Troubleshooting

### Application fails to start — MySQL connection refused
```
Ensure MySQL is running:
  macOS:   brew services start mysql
  Linux:   sudo systemctl start mysql
Check the URL and credentials in application.properties.
```

### Application fails to start — Artemis connection refused
```
Artemis must be running before the app starts.
Start it: /opt/artemis/urimaigal-broker/bin/artemis run
The broker URL must be tcp://localhost:61616 (not http://).
```

### Elasticsearch errors on startup (warnings only, app still works)
```
If ES is not running, LawyerSearchService logs a warning but does NOT
crash the app. MySQL fallback is used for all search queries.
You will see: "Could not initialise Elasticsearch index 'lawyers': ..."
```

### 403 Forbidden on protected endpoints
```
Include the JWT in every request:
  Authorization: Bearer <token-from-login-response>
Tokens expire after 24 hours (86400000 ms). Login again to get a new one.
```

### CORS errors in the browser
```
Add your frontend origin to application.properties:
  app.cors.allowed-origins=http://localhost:5173
Restart the server after changing this value.
```

### MySQL: "Table doesn't exist" on second startup
```
spring.sql.init.mode=always re-runs schema.sql on every startup.
All CREATE TABLE statements use IF NOT EXISTS so this is safe.
If you see duplicate key errors in data.sql, that's also safe —
INSERT IGNORE is used for the seed data.
```

### How to reset all data
```sql
USE urimaigal_db;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE chat_messages;
TRUNCATE TABLE lawyer_reviews;
TRUNCATE TABLE consultations;
TRUNCATE TABLE users;
TRUNCATE TABLE lawyers;
SET FOREIGN_KEY_CHECKS = 1;
```

Then restart the app — seed data will be reloaded from `data.sql`.
