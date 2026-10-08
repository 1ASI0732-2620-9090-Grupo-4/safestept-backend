# SafeStep API tests (Karate)

Black-box integration tests for the SafeStep REST API. They only talk HTTP, so they run against any
running instance: a local one, a Docker container started by the pipeline, or a deployed environment.

## Run

Start the API first (for example `mvn spring-boot:run` or the Docker image), then:

```bash
mvn test -Dapi.baseUrl=http://localhost:8092 \
         -Dapi.admin.username=<seeded admin> -Dapi.admin.password=<its password>
```

The defaults (`http://localhost:8093`, `apitest-admin`) match the isolated instance used in the CI pipeline.
The admin is the user created by the `SAFESTEP_ADMIN_USERNAME` / `SAFESTEP_ADMIN_PASSWORD` seed.

The HTML report is written to `target/karate-reports/karate-summary.html`.

## What is covered

| Feature | User stories | Scenarios |
| --- | --- | --- |
| `authentication/authentication.feature` | US01, US02 | sign-up (data-driven from `users-batch.json`), sign-in, refresh token rotation, conflicts, validation |
| `security/authorization.feature` | US57, US58 | 401/403 rules, role listing, role assignment, self-demotion protection |
| `catalog/catalog.feature` | US30, US31 | product, category, kit and coupon catalogues |
| `commerce/coupon-flow.feature` | US15, US40, US59, US60 | earn SafeCoins, redeem a coupon, create an order with the discount |
| `gamification/rewards.feature` | US15, US16 | rewards for completed simulations and score validation |

Every scenario creates its own users with random names, so the suite can be re-run against a database that
already contains data from previous runs.
