# Restaurant search service

## Projection

- Index: `restaurants_v1`; read/write alias: `restaurants`.
- One document per restaurant, `_id = restaurantId`.
- `menuItems` is `nested`, with no category fields or separate menu-item documents.
- Startup initializes the index/alias if missing; it does not delete old catalog indices.
- Local index settings use one shard and zero replicas; adjust replicas for production.

The consumer reads JSON payloads from `mealgo.restaurant.events` and
`mealgo.menu-item.events`. Records need an `eventType` header (`RestaurantCreated`
or `MenuItemCreated`), the aggregate UUID as key, and `occurredAt` in the payload.

Restaurant events patch restaurant fields without overwriting menu items. Menu
events upsert an array element by `menuItemId`. Scripted upserts handle menu events
arriving before restaurant events.
Repeated/older events are ignored using per-restaurant and per-item timestamps.
Equal timestamps are treated as duplicates; authoritative aggregate versions would
be needed to distinguish different updates sharing the same timestamp.

Only create events currently exist in the producer/consumer contract. Business
updates, deletes and status changes after creation are NOT synchronized yet.

## Scope

The service initializes the index, projects Kafka events into Elasticsearch, and
exposes nearby restaurant and dish-name search APIs. See [SEARCH_API.md](SEARCH_API.md)
for endpoints, response examples, and pagination constraints.

## Local run

Requires Java 21, Kafka and Elasticsearch.

```powershell
.\search-service\mvnw.cmd -f common-web/pom.xml install -DskipTests
.\search-service\mvnw.cmd -f search-service/pom.xml spring-boot:run
```

| Environment variable | Default |
| --- | --- |
| KAFKA_BOOTSTRAP_SERVERS | localhost:29090 |
| KAFKA_CONSUMER_GROUP | search-catalog-indexer |
| ELASTICSEARCH_URIS | http://localhost:29200 |

`app.search.index` defaults to `restaurants`. Bootstrap currently uses the fixed
`restaurants` alias and `restaurants_v1` index; a custom alias must be provisioned
separately before overriding that property.

### Existing data

The new index starts empty. Restarting with the old consumer group does not replay
committed events. To rebuild from retained events, stop the old instance and start
with a dedicated new `KAFKA_CONSUMER_GROUP`, after confirming both topics contain
the required history. A new group reads from earliest available offsets. Do not
run the old catalog writer with the same group while rebuilding.

Replay cannot restore events removed by retention, and creation events do not
represent subsequent database changes. A full backfill from current source data
is required in those cases; no automated database backfill is included here.

## Tests

```powershell
.\search-service\mvnw.cmd -f search-service/pom.xml test
# Optional: creates and deletes ONLY a unique test index in local Elasticsearch.
$env:RUN_SEARCH_INTEGRATION_TESTS = 'true'
$env:TEST_ELASTICSEARCH_URL = 'http://localhost:29200'
.\search-service\mvnw.cmd -f search-service/pom.xml test
```
