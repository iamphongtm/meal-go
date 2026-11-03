package com.mealgo.searchservice.infrastructure.elasticsearch.mapping;

import com.mealgo.searchservice.domain.CatalogDocument;
import com.mealgo.searchservice.domain.CatalogType;

import java.util.Map;

public interface CatalogDocumentMapper {
    CatalogType supportedType();

    Map<String, Object> toBody(CatalogDocument document);
}
