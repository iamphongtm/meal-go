package com.mealgo.searchservice.infrastructure.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.mealgo.searchservice.application.port.CatalogIndexPort;
import com.mealgo.searchservice.domain.CatalogDocument;
import com.mealgo.searchservice.infrastructure.elasticsearch.mapping.CatalogDocumentMapperFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ElasticsearchCatalogAdapter implements CatalogIndexPort {
    @Value("${app.search.index:search_catalog}")
    private String indexAlias;

    private final ElasticsearchClient client;
    private final CatalogDocumentMapperFactory mapperFactory;

    @Override
    public void upsert(CatalogDocument document) {
        Map<String, Object> body = mapperFactory.getMapper(document.type()).toBody(document);

        try {
            client.index(request -> request
                    .index(indexAlias)
                    .requireAlias(true)
                    .id(document.documentId())
                    .document(body)
            );
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Cannot index document " + document.documentId(), e
            );
        }
    }
}
