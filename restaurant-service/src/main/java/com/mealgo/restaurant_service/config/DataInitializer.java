package com.mealgo.restaurant_service.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final String PROVINCES_API = "https://provinces.open-api.vn/api/v2/p/";
    private static final String WARDS_API = "https://provinces.open-api.vn/api/v2/w/";

    private final JdbcTemplate jdbcTemplate;
    private final RestClient restClient = RestClient.create();

    public DataInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedProvinces();
        seedWards();
    }

    private void seedProvinces() {
        List<ProvinceData> provinces = restClient.get()
                .uri(PROVINCES_API)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (provinces == null || provinces.isEmpty()) {
            throw new IllegalStateException("Provinces API returned no data");
        }

        jdbcTemplate.batchUpdate("""
                INSERT INTO provinces (code, name, division_type, codename, phone_code)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (code) DO UPDATE SET
                    name = EXCLUDED.name,
                    division_type = EXCLUDED.division_type,
                    codename = EXCLUDED.codename,
                    phone_code = EXCLUDED.phone_code
                """, provinces, 100, (statement, province) -> {
            statement.setInt(1, province.code());
            statement.setString(2, province.name());
            statement.setString(3, province.divisionType());
            statement.setString(4, province.codename());
            statement.setInt(5, province.phoneCode());
        });
    }

    private void seedWards() {
        List<WardData> wards = restClient.get()
                .uri(WARDS_API)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (wards == null || wards.isEmpty()) {
            throw new IllegalStateException("Wards API returned no data");
        }

        jdbcTemplate.batchUpdate("""
                INSERT INTO wards (code, name, division_type, codename, province_code)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (code) DO UPDATE SET
                    name = EXCLUDED.name,
                    division_type = EXCLUDED.division_type,
                    codename = EXCLUDED.codename,
                    province_code = EXCLUDED.province_code
                """, wards, 500, (statement, ward) -> {
            statement.setInt(1, ward.code());
            statement.setString(2, ward.name());
            statement.setString(3, ward.divisionType());
            statement.setString(4, ward.codename());
            statement.setInt(5, ward.provinceCode());
        });
    }

    private record ProvinceData(
            String name,
            Integer code,
            @JsonProperty("division_type") String divisionType,
            String codename,
            @JsonProperty("phone_code") Integer phoneCode) {
    }

    private record WardData(
            String name,
            Integer code,
            @JsonProperty("division_type") String divisionType,
            String codename,
            @JsonProperty("province_code") Integer provinceCode) {
    }
}
