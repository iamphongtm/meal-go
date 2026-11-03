package com.mealgo.searchservice.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealgo.searchservice.application.IndexOutboxEventUseCase;
import com.mealgo.searchservice.domain.CatalogEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxKafkaConsumer {

    private final IndexOutboxEventUseCase indexer;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {
            "${app.kafka.inbound-topic.restaurant}",
            "${app.kafka.inbound-topic.menu-item}"
    })
    public void consume(ConsumerRecord<String, String> record) throws JsonProcessingException {
        if(record.value() == null){
            return;
        }

        Header header = record.headers().lastHeader("eventType");
        log.info("eventType: {}", header);

        if(header == null || header.value() == null){
            throw new IllegalArgumentException("Missing eventType header");
        }
        String eventName = new String(header.value(), StandardCharsets.UTF_8);

        CatalogEventType eventType = CatalogEventType.fromExternal(eventName)
                .orElseThrow(() ->  new IllegalArgumentException("Unknown event type: " + eventName));

        UUID aggregateId = UUID.fromString(record.key());

        JsonNode payload = objectMapper.readTree(record.value());
        Instant occurredAt = Instant.parse(payload.get("occurredAt").asText());

        indexer.handle(eventType, aggregateId, occurredAt, payload);
    }
}
