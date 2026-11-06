# Local restaurant/search infrastructure

Requires Docker with Compose **2.24+** (`include` and optional `env_file` support), JDK 21, and sufficient
Docker memory (allow at least 4 GB). This stack is for local development only:
Elasticsearch/Kafka/management UIs have no authentication and PostgreSQL uses
development credentials. Published ports bind only to `127.0.0.1`.

## Start

Run from the repository root:

```powershell
Copy-Item .env.example .env
docker compose config --quiet
docker compose build restaurant-service search-service
docker compose up -d
docker compose ps
```

Do not overwrite an existing `.env`. The Compose project is `mealgo-personal`,
with separate named volumes from the team stack. Default host ports still
conflict if the team stack is running: stop that stack without removing volumes,
or change **all conflicting ports** in `.env` before starting this one.

| Component | Default host endpoint |
| --- | --- |
| PostgreSQL | `localhost:5432` |
| Kafka (host clients) | `localhost:29090` |
| Kafka UI | `http://localhost:28080` |
| Kafka Connect | `http://localhost:28083` |
| Elasticsearch | `http://localhost:29200` |
| Kibana | `http://localhost:25601` |
| Restaurant API | `http://localhost:8030` |
| Search API | `http://localhost:8090` |

Containers use `kafka:9092` and `restaurant-db:5432`; the Debezium connector
configuration already targets these internal endpoints. Host ports may change
without changing connector JSON.

## Containerized Java applications

Compose now includes both Java services using `infra/docker/java-service.Dockerfile`.
Before starting them, create `restaurant-service/.env` from
`restaurant-service/.env.example` and configure your own AWS values. Do not
overwrite an existing file or commit credentials. Stop IDE/Maven instances using
the same HTTP ports. Set `RESTAURANT_SERVICE_PORT` and `SEARCH_SERVICE_PORT` in the
root `.env` to override the default host ports; all bindings remain localhost-only.

Applications use internal database, Kafka and Elasticsearch endpoints. Builds use
JDK 21 and a non-root JRE runtime, exclude local secrets/build artifacts, and skip
tests; run Maven tests separately. Starting the applications does not automatically
register Debezium or reset any data. Configure the connector after restaurant-service
has created the outbox table, as described below.

To start infrastructure only, explicitly select `restaurant-db`, `kafka`, `kafka-ui`,
`kafka-connect`, `elasticsearch` and `kibana` in the `docker compose up -d` command.

## Run Java applications with IDE/Maven instead

Do not start containerized copies of the applications at the same time. Set
`JAVA_HOME` to JDK 21 and build/install the reactor before launching independently:

```powershell
.\restaurant-service\mvnw.cmd -f pom.xml install -DskipTests
```

In the terminal/IDE that runs restaurant-service, configure:

```powershell
$env:SPRING_DATASOURCE_URL = 'jdbc:postgresql://localhost:5432/mealgo_restaurant'
$env:SPRING_DATASOURCE_USERNAME = 'restaurant'
$env:SPRING_DATASOURCE_PASSWORD = 'restaurant'
# Also provide AWS_ACCESS_KEY, AWS_SECRET_KEY, AWS_S3_BUCKET, AWS_REGION,
# and AWS_CDN_BASE_URL for your own S3 environment. Never commit credentials.
.\restaurant-service\mvnw.cmd -f restaurant-service/pom.xml spring-boot:run
```

Use the configured database port if changed. Restaurant HTTP port is `8030`.
The new database starts empty; Flyway creates/seeds the schema on application
startup. Do not point this configuration at an old database without reviewing
migration compatibility first.

After restaurant-service has successfully migrated the database, open another
terminal and register the CDC connector:

```powershell
.\infra\debezium\register-connector.ps1
Invoke-RestMethod http://localhost:28083/connectors/restaurant-outbox/status |
    ConvertTo-Json -Depth 10
```

Verify both connector and task states are `RUNNING`. Configuration submission is
not proof that the connector task started. If you changed the Connect port, pass
`-ConnectUrl http://localhost:YOUR_PORT` and use that port for status checks.

Start search-service in another terminal/IDE:

```powershell
$env:KAFKA_BOOTSTRAP_SERVERS = 'localhost:29090'
$env:ELASTICSEARCH_URIS = 'http://localhost:29200'
.\search-service\mvnw.cmd -f search-service/pom.xml spring-boot:run
```

Update these variables if you changed `.env` ports. Compose reads `.env` for
interpolation; that file does not automatically export variables into Java/IDE.
Search HTTP port is `8090`.

## Verify and stop

Create a restaurant through the API with valid latitude/longitude. Check the
outbox topic in Kafka UI, then run in Kibana Dev Tools:

```http
GET /search_catalog/_search
{
  "query": { "term": { "type": "RESTAURANT" } },
  "sort": [{ "updatedAt": "desc" }]
}
```

The document should contain `location.lat` and `location.lon`. For an existing
Elasticsearch volume, see `docs/search-restaurant-sync.md` for mapping/backfill.

```powershell
docker compose logs --tail=100 kafka-connect
docker compose stop
# Or remove containers/network while retaining named data volumes:
docker compose down
```

Do not use `down -v` unless you intentionally want to delete local database,
Kafka, and Elasticsearch data. No automatic volume deletion is required.
