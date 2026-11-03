# Restaurant and search service synchronization

This branch imports a current working-tree snapshot of `restaurant-service` and
`search-service` from `hungday12334/meal-go-platform`, branch `search-service`,
based on commit `c96d6e9`, including local service changes at import time.
It does not recreate or backdate the source repository's commit history.

The personal repository retains its root-level service layout. The shared
`common-web` module and a Maven parent are included because the imported services
depend on them. Other existing services are unchanged and are not part of this
Maven reactor.

## Build and test

Use JDK 21, set `JAVA_HOME`, then run from the repository root:

```powershell
.\restaurant-service\mvnw.cmd -f pom.xml test
```

## Runtime dependencies

- Restaurant: PostgreSQL, datasource environment variables, AWS/S3 configuration.
- Search: Kafka and Elasticsearch. Defaults are `localhost:29090` and
  `http://localhost:29200`, respectively; see `search-service/src/main/resources/application.yaml`.
- CDC: `infra/debezium/restaurant-outbox.json` is the source development connector
  configuration. Its database hostname and credentials are development defaults;
  adapt them to the deployment and do not use them as production credentials.
- Restaurant messages route to `mealgo.restaurant.events`; menu-item messages
  route to `mealgo.menu-item.events`.

The restaurant service now uses Flyway migrations. For an existing database from
the older personal service, back up the database and review schema compatibility
before starting; do not blindly baseline or run migrations against old data.

## Coordinates and existing Elasticsearch data

Events contain `latitude` and `longitude`; the search mapper converts these into
`location: { "lat": ..., "lon": ... }`. Both coordinates must be present.

The Java initializer bootstraps a new index but does not migrate an existing
index when the alias already exists. Add the field explicitly when upgrading:

```http
PUT /search_catalog_v1/_mapping
{
  "properties": {
    "location": { "type": "geo_point" }
  }
}
```

Existing documents require a separate backfill or controlled event replay.
Restarting the Kafka consumer does not replay already committed offsets.

## Import adjustments

- Adapted Maven parent paths and module list to the personal repository layout.
- Pinned the Maven compiler plugin version.
- Removed credential-like comments from the imported AWS configuration.
- Updated the restaurant controller test payload and assertions for coordinates.

Verification: Maven reactor tests passed with 21 restaurant tests; the imported
search service currently has no automated tests. This is not an end-to-end
runtime validation of PostgreSQL, Kafka, Debezium, S3, or Elasticsearch.
