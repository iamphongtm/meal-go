package com.mealgo.searchservice.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchIndexConfiguration {

    @Bean(initMethod = "initialize")
    public ElasticsearchIndexInitializer catalogIndexInitializer(
            ElasticsearchClient client
    ) {
        return new ElasticsearchIndexInitializer(client);
    }
}
