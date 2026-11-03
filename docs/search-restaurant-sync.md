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

Events contain `latitude` and `longitude`; the search index adapter converts these into
`location: { "lat": ..., "lon": ... }`. Both coordinates must be present.

The current projection uses `restaurants_v1` with alias `restaurants`, one document
per restaurant, and nested `menuItems`. It replaces the old separate catalog
documents; adding a mapping field to `search_catalog_v1` alone is not a migration.
The Java initializer creates the new index/alias when missing but does not delete
old indices or migrate existing documents. See `search-service/README.md` for
controlled replay and retention limitations.

Existing documents require a separate backfill or controlled event replay.
Restarting the Kafka consumer does not replay already committed offsets.

## Import adjustments

- Adapted Maven parent paths and module list to the personal repository layout.
- Pinned the Maven compiler plugin version.
- Removed credential-like comments from the imported AWS configuration.
- Updated the restaurant controller test payload and assertions for coordinates.

## Follow-up synchronization

Imported search-service changes from source commit `c258247` on branch
`search-service`, with the user's permission to publish these services. The source
frontend commit `cabf171` is deliberately outside this synchronization scope.
Restaurant code already matches apart from personal build/configuration and test
adjustments, which are retained. No infrastructure or common-web changes are needed.

- Replaced catalog documents with restaurant projections and nested menu items.
- Added per-entity timestamp checks for duplicate/out-of-order events.
- Added nearby and dish-name search APIs, including up to five matching dishes.
- Added search unit tests and an opt-in Elasticsearch integration test.
- Retained root-level service layout, Maven parent paths, compiler version,
  sanitized configuration, and restaurant coordinate test assertions.

See `search-service/SEARCH_API.md` for API details. Tests do not constitute
end-to-end validation of PostgreSQL, Kafka, Debezium, S3, or Elasticsearch.

Follow-up verification: the JDK 21 Maven reactor passed with 21 restaurant tests
and 13 search tests passing. One opt-in Elasticsearch integration test was skipped.
`git diff --check` passed. No infrastructure stack or connector was started or changed.
