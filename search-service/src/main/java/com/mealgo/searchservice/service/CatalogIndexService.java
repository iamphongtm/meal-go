package com.mealgo.searchservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mealgo.searchservice.model.CatalogEventType;

import java.time.Instant;
import java.util.UUID;

public interface CatalogIndexService {
    void handle(CatalogEventType eventType, UUID aggregateId, Instant occurredAt, JsonNode payload);
}
