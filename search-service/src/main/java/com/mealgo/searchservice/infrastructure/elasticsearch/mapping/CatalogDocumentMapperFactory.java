package com.mealgo.searchservice.infrastructure.elasticsearch.mapping;

import com.mealgo.searchservice.domain.CatalogType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CatalogDocumentMapperFactory {

    private final Map<CatalogType, CatalogDocumentMapper> mappers;

    public CatalogDocumentMapperFactory(List<CatalogDocumentMapper> mappers) {
        this.mappers = mappers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        CatalogDocumentMapper::supportedType,
                        Function.identity()
                ));
    }

    public CatalogDocumentMapper getMapper(CatalogType type) {
        CatalogDocumentMapper mapper = type == null ? null : mappers.get(type);
        if (mapper == null) {
            throw new IllegalArgumentException("Unsupported catalog type: " + type);
        }
        return mapper;
    }
}
