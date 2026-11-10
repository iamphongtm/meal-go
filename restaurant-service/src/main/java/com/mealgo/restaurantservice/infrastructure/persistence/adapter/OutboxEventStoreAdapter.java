package com.mealgo.restaurantservice.infrastructure.persistence.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealgo.restaurantservice.application.event.AggregateType;
import com.mealgo.restaurantservice.application.event.RestaurantAddressUpdatedEvent;
import com.mealgo.restaurantservice.application.event.MenuItemCreatedEvent;
import com.mealgo.restaurantservice.application.event.RestaurantCreatedEvent;
import com.mealgo.restaurantservice.application.port.out.OutboxEventStore;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.OutboxEventJpaEntity;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.OutboxEventJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventStoreAdapter implements OutboxEventStore {
    @Override
    public void append(com.mealgo.restaurantservice.application.event.RestaurantApprovedEvent event) {
        outboxEventRepo.save(OutboxEventJpaEntity.builder()
                .id(event.eventId()).aggregateId(event.restaurantId())
                .aggregateType(AggregateType.RESTAURANT.value()).eventType(event.eventType())
                .payload(serialize(event)).createdAt(event.occurredAt()).build());
    }
    private final OutboxEventJpaRepository outboxEventRepo;
    private final ObjectMapper objectMapper;

    @Override
    public void append(RestaurantAddressUpdatedEvent event) {
        outboxEventRepo.save(OutboxEventJpaEntity.builder()
                .id(event.eventId())
                .aggregateId(event.restaurantId())
                .aggregateType(AggregateType.RESTAURANT.value())
                .eventType(event.eventType())
                .payload(serialize(event))
                .createdAt(event.occurredAt())
                .build());
    }

    @Override
    public void append(MenuItemCreatedEvent event) {
        outboxEventRepo.save(OutboxEventJpaEntity.builder()
                .id(event.eventId())
                .aggregateId(event.menuItemId())
                .aggregateType(AggregateType.MENU_ITEM.value())
                .eventType(event.eventType())
                .payload(serialize(event))
                .createdAt(event.occurredAt())
                .build());
    }

    @Override
    public void append(RestaurantCreatedEvent event) {
        outboxEventRepo.save(OutboxEventJpaEntity.builder()
                .id(event.eventId())
                .aggregateId(event.restaurantId())
                .aggregateType(AggregateType.RESTAURANT.value())
                .eventType(event.eventType())
                .payload(serialize(event))
                .createdAt(event.occurredAt())
                .build());
    }

    private String serialize(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Cannot serialize " + event.getClass().getSimpleName(), ex);
        }
    }
}
