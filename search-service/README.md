# Search catalog indexer

Consumes raw Debezium JSON from `restaurant.public.outbox_events` and writes
`RestaurantCreated` / `MenuItemCreated` (also accepts `RESTAURANT_CREATED` /
`MENU_ITEM_CREATED`) to the Elasticsearch alias `search_catalog`.
No search REST API or business update/delete handlers are implemented yet.

## Local setup

Start Kafka, Elasticsearch and Kafka Connect using the root Compose configuration.
The PostgreSQL publication and Debezium connector must already be configured.

Before starting the service, create the index only if it does not already exist:

```powershell
Invoke-WebRequest http://localhost:29200/search_catalog_v1 -Method Head
# If 404 (and search_catalog is not already assigned), create it:
$mapping = Get-Content services/search-service/src/main/resources/elasticsearch/search_catalog_v1.json -Raw -Encoding UTF8
Invoke-RestMethod -Uri http://localhost:29200/search_catalog_v1 -Method Put -ContentType 'application/json; charset=utf-8' -Body ([System.Text.Encoding]::UTF8.GetBytes($mapping))
```

Do not delete/recreate an existing index. The mapping uses local single-node
settings (one shard, no replica). The adapter requires an alias to avoid accidentally
creating an unmapped index. Mapping changes are not automatically applied.

Run from the repository root with Java 21 and Maven:

```powershell
mvn -f services/search-service/pom.xml test '-Dtest=DebeziumOutboxParserTest,IndexOutboxEventUseCaseTest,OutboxKafkaConsumerTest'
mvn -f services/search-service/pom.xml spring-boot:run
```

Environment variables:

| Variable | Default on host |
| --- | --- |
| KAFKA_BOOTSTRAP_SERVERS | localhost:29090 |
| KAFKA_CONSUMER_GROUP | search-catalog-indexer |
| ELASTICSEARCH_URIS | http://localhost:29200 |
| SEARCH_INDEX | search_catalog |
| SEARCH_OUTBOX_TOPIC | restaurant.public.outbox_events |

In Docker, use `kafka:9092` and `http://elasticsearch:9200`.
The consumer starts from earliest only when its group has no committed offset.
Changing the group replays retained events; it cannot recover events already
removed by Kafka retention. Historical menu events may lack restaurantName;
they are indexed without it and need a separate backfill if required.

## Processing and errors

Record offsets are acknowledged after successful indexing, or after successful
dead-letter publication. Invalid payloads go directly to `<source-topic>.DLT`;
other failures get three retries two seconds apart, then go to DLT. DLT publish
failures are propagated rather than treated as recovered. Monitor and replay
the DLT after fixing errors, especially after an Elasticsearch outage.
DLT has one partition/replica for the local environment.

Tombstones, outbox row updates/deletes and unsupported event types are ignored.
Outbox cleanup is not a business entity deletion. Fixed document IDs make replay
of the same create event overwrite the same document, not duplicate it. This is
not exactly-once processing or protection against out-of-order business updates.
Add aggregate versions before implementing update events.

Create a real restaurant/menu item through the restaurant API, then inspect:

```powershell
Invoke-RestMethod http://localhost:29200/search_catalog/_count
Invoke-RestMethod http://localhost:29200/search_catalog/_search
```

Search visibility may lag indexing until Elasticsearch refreshes. The test event
`TEST_CREATED` is ignored. Restaurant renames/status changes and entity deletes
will not synchronize until the corresponding business events and handlers exist.
