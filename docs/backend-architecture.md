# SafeStep Backend Architecture

SafeStep Backend follows the same course backend style: Spring Boot REST controllers, application command/query services, domain-friendly bounded contexts, infrastructure adapters and shared cross-cutting modules.

## Contexts

- `iam`: JWT authentication, users and roles.
- `profiles`: SafeStep profile data for the authenticated user.
- `simulation`: first-aid simulation catalog and attempts.
- `gamification`: missions, badges, coins, XP and leaderboard.
- `commerce`: products, emergency kits, cart, orders, coupons and recommendations.
- `analytics`: progress, statistics and certificates.
- `shared`: result/error model, auditing, OpenAPI, i18n and persistence naming.

## Persistence

Each bounded context uses typed JPA persistence entities, Spring Data repositories, domain repository ports and infrastructure adapters. Seed handlers read `safestep-seed.json` and import the initial frontend data into context-specific tables such as products, simulations, missions, badges, certificates and related records.

Domain aggregates and value objects do not depend on JPA annotations. Persistence-specific classes remain under each context's `infrastructure/persistence/jpa` package, and conversion between persistence and domain models is handled by repository adapters or context-specific persistence assemblers.

## Security

All endpoints are authenticated except:

- `/api/v1/authentication/**`
- `/v3/api-docs/**`
- `/swagger-ui/**`
- Swagger and webjar support routes

Authenticated requests must send:

```text
Authorization: Bearer <token>
```
