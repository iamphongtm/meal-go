package com.mealgo.restaurant_service.exception;

import com.mealgo.restaurant_service.dto.request.RestaurantCreationRequest;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n/messages");

        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler(messageSource);
        ReflectionTestUtils.setField(exceptionHandler, "servicename", "restaurant-service");

        mockMvc = MockMvcBuilders.standaloneSetup(new ValidationController())
                .setControllerAdvice(exceptionHandler)
                .setValidator(createValidator(messageSource))
                .build();
    }

    @Test
    void returnsValidationProblemDetailForInvalidPhone() throws Exception {
        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Test Restaurant",
                                  "phone": "0966680805",
                                  "provinceCode": 1,
                                  "wardCode": 1,
                                  "addressLine": "123 Test Street"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:mealgo:restaurant-service:validation-error"))
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Input validation failed."))
                .andExpect(jsonPath("$.instance").value("/restaurants"))
                .andExpect(jsonPath("$.invalid_params[0].field").value("phone"))
                .andExpect(jsonPath("$.invalid_params[0].message")
                        .value("Phone must be in international format, for example +84901234567"));
    }

    @Test
    void returnsProblemDetailForUnexpectedException() throws Exception {
        mockMvc.perform(get("/restaurants/error"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type")
                        .value("urn:mealgo:restaurant-service:internal-server-error"))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred."))
                .andExpect(jsonPath("$.instance").value("/restaurants/error"));
    }

    private static org.springframework.validation.Validator createValidator(ResourceBundleMessageSource messageSource) {
        org.springframework.validation.beanvalidation.LocalValidatorFactoryBean validator =
                new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        validator.afterPropertiesSet();
        return validator;
    }

    @RestController
    @RequestMapping("/restaurants")
    private static class ValidationController {
        @PostMapping
        void create(@Valid @RequestBody RestaurantCreationRequest request) {
        }

        @GetMapping("/error")
        void fail() {
            throw new IllegalStateException("Database unavailable");
        }
    }
}
