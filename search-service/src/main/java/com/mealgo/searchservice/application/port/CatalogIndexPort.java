package com.mealgo.searchservice.application.port;

import com.mealgo.searchservice.domain.CatalogDocument;

public interface CatalogIndexPort {
    void upsert(CatalogDocument document);
}
