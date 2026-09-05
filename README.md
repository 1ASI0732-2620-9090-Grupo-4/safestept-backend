# SafeStep Backend

SafeStep Backend is the REST API for the SafeStep first-aid learning platform. It follows the same Spring Boot and DDD-oriented structure used in the course backend example: bounded contexts, command/query services, REST resources, JPA persistence, JWT security, OpenAPI documentation and localized error handling.

## Stack

- Java 26
- Spring Boot 4
- Maven
- Spring Web, Spring Data JPA, Validation
- Spring Security + JWT + BCrypt
- PostgreSQL
- SpringDoc OpenAPI
- Stripe Checkout

## Bounded Contexts

- `iam`: authentication, users, roles and JWT.
- `profiles`: SafeStep user profile endpoints.
- `simulation`: medical simulation catalog and attempts.
- `gamification`: XP, coins, missions, badges and leaderboard.
- `commerce`: products, kits, cart, orders, coupons and recommendations.
- `analytics`: progress, statistics and certificates.
- `shared`: auditing, result objects, error responses, OpenAPI, i18n and shared persistence conventions.

## Run Locally

### 1. Requirements

Install and verify:

- Java JDK 26
- Apache Maven 3.9 or newer
- PostgreSQL 18 or a compatible version

```powershell
java -version
javac -version
mvn -version
psql --version
```

Maven must report that it is using Java 26.

### 2. Create The Database

Create a PostgreSQL database named `safestep`.

From PowerShell:

```powershell
$env:PGPASSWORD = "postgres"
psql -h localhost -p 5432 -U postgres -d postgres -c "CREATE DATABASE safestep;"
```

Alternatively, use pgAdmin:

1. Connect to the PostgreSQL 18 server.
2. Right-click `Databases`.
3. Select `Create > Database`.
4. Use `safestep` as the database name and `postgres` as owner.

The tables are generated automatically by Hibernate when the backend starts.

### 3. Configure Environment Variables

The default development configuration expects these values:

```powershell
$env:DATABASE_URL = "localhost"
$env:DATABASE_PORT = "5432"
$env:DATABASE_NAME = "safestep"
$env:DATABASE_USER = "postgres"
$env:DATABASE_PASSWORD = "postgres"
$env:JWT_SECRET = "replace-with-a-long-secret-key"
$env:PORT = "8092"
```

If you are using the Render PostgreSQL database, replace the database values with the host, database name, user and password provided by Render:

```powershell
$env:DATABASE_URL = "your-render-postgres-host"
$env:DATABASE_PORT = "5432"
$env:DATABASE_NAME = "your-render-database-name"
$env:DATABASE_USER = "your-render-database-user"
$env:DATABASE_PASSWORD = "your-render-database-password"
$env:JWT_SECRET = "replace-with-a-long-secret-key"
$env:PORT = "8092"
```

### 4. Configure Stripe Checkout

For normal development without testing payments, the backend can start without Stripe variables. To test payments, define:

```powershell
$env:STRIPE_SECRET_KEY = "sk_test_your_secret_key"
$env:STRIPE_WEBHOOK_SECRET = "whsec_your_webhook_secret"
$env:STRIPE_CURRENCY = "usd"
$env:STRIPE_SUCCESS_URL = "http://localhost:4200/payment/success"
$env:STRIPE_CANCEL_URL = "http://localhost:4200/payment/cancel"
```

To obtain the webhook secret in local development, start Stripe CLI in another terminal:

```powershell
stripe login
stripe listen --forward-to localhost:8092/api/v1/commerce/payments/stripe/webhook
```

Copy the `whsec_...` value printed by Stripe CLI into `STRIPE_WEBHOOK_SECRET`.

Never commit real `sk_test_...`, `sk_live_...` or `whsec_...` values to GitHub.

### 5. Start The Backend

Open PowerShell in the `safestep-backend` folder:

```powershell
cd C:\path\to\TrabajoFinalOpenSource\safestep-backend
mvn spring-boot:run
```

Wait until the console reports that `SafeStepPlatformApplication` started.

The API will be available at:

```text
http://localhost:8092
```

Swagger UI:

```text
http://localhost:8092/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8092/v3/api-docs
```

### 6. Run Tests

```powershell
mvn test
```

The expected result is `BUILD SUCCESS`.

### 7. Build And Run The JAR

```powershell
mvn clean package
java -jar target\safestep-platform-1.0.0.jar
```

### 8. Stop The Backend

When running in the current terminal, press:

```text
Ctrl + C
```

## Main Endpoints

- `POST /api/v1/authentication/sign-in`
- `POST /api/v1/authentication/sign-up`
- `POST /api/v1/authentication/refresh-token`
- `POST /api/v1/authentication/logout`
- `POST /api/v1/authentication/forgot-password`
- `POST /api/v1/authentication/reset-password`
- `GET /api/v1/profiles/me`
- `PUT /api/v1/profiles/me`
- `GET /api/v1/simulations`
- `GET /api/v1/simulations/{simulationId}`
- `POST /api/v1/simulations/{simulationId}/attempts`
- `GET /api/v1/simulations/attempts/me`
- `GET /api/v1/gamification/summary/me`
- `GET /api/v1/gamification/missions`
- `GET /api/v1/gamification/badges/me`
- `GET /api/v1/gamification/leaderboard`
- `GET /api/v1/gamification/coin-transactions/me`
- `GET /api/v1/commerce/products`
- `GET /api/v1/commerce/products/{productId}`
- `GET /api/v1/commerce/categories`
- `GET /api/v1/commerce/kits`
- `GET /api/v1/commerce/cart/me`
- `POST /api/v1/commerce/cart/items`
- `PUT /api/v1/commerce/cart/items/{itemId}`
- `DELETE /api/v1/commerce/cart/items/{itemId}`
- `GET /api/v1/commerce/orders/me`
- `POST /api/v1/commerce/orders`
- `POST /api/v1/commerce/orders/{orderId}/payments/stripe-checkout`
- `POST /api/v1/commerce/payments/stripe/webhook`
- `GET /api/v1/commerce/coupons`
- `GET /api/v1/commerce/recommendations/me`
- `GET /api/v1/analytics/summary/me`
- `GET /api/v1/analytics/progress/me`
- `GET /api/v1/analytics/certificates/me`

## Seed Data

`src/main/resources/safestep-seed.json` is copied from the current Angular frontend mock database. Each bounded context imports only its own typed data through an idempotent seed handler.

## DDD Architecture

The `simulation`, `gamification`, `commerce`, and `analytics` contexts follow the same dependency direction as the course example:

```text
domain -> application -> infrastructure -> interfaces
```

Domain aggregates and value objects contain no JPA annotations. Persistence is implemented with context-specific entities, Spring Data repositories, adapters and assemblers. Cross-context reads use ACL facades, while completed simulations notify gamification through an integration event.

The former generic `safestep_resources` JSON persistence has been removed. REST controllers and application services use typed commands and resources.
