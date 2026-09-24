package com.mealgo.restaurant_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI restaurantServiceOpenApi(
            @Value("${app.api.version}") String apiVersion
    ) {
        return new OpenAPI()
                .info(new Info()
                        .title("MealGo Restaurant Service API")
                        .version(apiVersion)
                        .description("""
                                APIs for initializing and managing restaurants.
                                """)
                        .contact(new Contact()
                                .name("MealGo Development Team")));
    }
}
