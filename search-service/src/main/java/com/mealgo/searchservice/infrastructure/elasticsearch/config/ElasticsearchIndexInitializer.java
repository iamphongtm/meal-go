package com.mealgo.searchservice.infrastructure.elasticsearch.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
public class ElasticsearchIndexInitializer {
    private final ElasticsearchClient client;

    private static final String ALIAS = "search_catalog";
    private static final String INITIAL_INDEX = "search_catalog_v1";
    private static final String MAPPING_RESOURCE =
            "elasticsearch/search_catalog_v1.json";

    public void initialize() throws IOException {
        // 1. Kiểm tra alias.
        boolean aliasExists = client.indices()
                .existsAlias(request -> request.name(ALIAS))
                .value();

        // 2. Alias có rồi: kiểm tra đích ghi, không tự chuyển về v1.
        if (aliasExists) {
            validateWriteAlias();
            return;
        }

        // 3. Kiểm tra tên alias có bị index thật chiếm dụng không.
        boolean aliasNameOccupied = client.indices()
                .exists(request -> request.index(ALIAS))
                .value();

        if (aliasNameOccupied) {
            throw new IllegalStateException(
                    "Không thể tạo alias " + ALIAS
                            + ": tên này đang được sử dụng bởi một index"
            );
        }

        // 4. Kiểm tra index khởi tạo.
        boolean indexExists = client.indices()
                .exists(request -> request.index(INITIAL_INDEX))
                .value();

        if (!indexExists) {
            // 5. Chưa có index: tạo từ JSON, bao gồm alias.
            createInitialIndex();
        } else {
            // 6. Có index: kiểm tra mapping rồi gắn alias.
            validateInitialIndexMapping();
            attachAlias();
        }

        // Kiểm tra lại trạng thái cuối cùng.
        validateWriteAlias();
    }

    private void createInitialIndex() throws IOException {
        ClassPathResource resource =
                new ClassPathResource(MAPPING_RESOURCE);

        try (InputStream input = resource.getInputStream()) {
            var response = client.indices().create(request -> request
                    .index(INITIAL_INDEX)
                    .withJson(input)
            );

            if (!response.acknowledged()) {
                throw new IllegalStateException(
                        "Chưa nhận được xác nhận tạo index "
                                + INITIAL_INDEX
                                + ". Cần kiểm tra lại trạng thái Elasticsearch."
                );
            }
        } catch (ElasticsearchException exception) {
            // Có thể instance khác vừa tạo index sau bước kiểm tra.
            // Chỉ xử lý riêng lỗi đã tồn tại, không nuốt các lỗi khác.
            if (!"resource_already_exists_exception"
                    .equals(exception.error().type())) {
                throw exception;
            }

            boolean aliasExists = client.indices()
                    .existsAlias(request -> request.name(ALIAS))
                    .value();

            if (aliasExists) {
                validateWriteAlias();
                return;
            }

            validateInitialIndexMapping();
            attachAlias();
        }
    }

    private void validateInitialIndexMapping() throws IOException {
        TypeMapping expected = readExpectedMapping();

        var response = client.indices()
                .getMapping(request -> request.index(INITIAL_INDEX));

        var indexMapping = response.result().get(INITIAL_INDEX);

        if (indexMapping == null) {
            throw new IllegalStateException(
                    "Không lấy được mapping của index " + INITIAL_INDEX
            );
        }

        TypeMapping actual = indexMapping.mappings();

        // So sánh cấu trúc JSON thay vì so sánh object Java.
        var objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

        var expectedJson = objectMapper.readTree(toJson(expected));
        var actualJson = objectMapper.readTree(toJson(actual));

        if (!expectedJson.equals(actualJson)) {
            throw new IllegalStateException(
                    "Mapping của " + INITIAL_INDEX
                            + " khác file " + MAPPING_RESOURCE
                            + ". Không tự động gắn alias hoặc sửa mapping."
            );
        }
    }

    private TypeMapping readExpectedMapping() throws IOException {
        ClassPathResource resource =
                new ClassPathResource(MAPPING_RESOURCE);

        var objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

        try (InputStream input = resource.getInputStream()) {
            var root = objectMapper.readTree(input);
            var mappings = root.get("mappings");

            if (mappings == null || !mappings.isObject()) {
                throw new IllegalStateException(
                        "File " + MAPPING_RESOURCE
                                + " không có mappings hợp lệ"
                );
            }

            return new TypeMapping.Builder()
                    .withJson(new java.io.StringReader(mappings.toString()))
                    .build();
        }
    }

    private String toJson(TypeMapping mapping) {
        var writer = new java.io.StringWriter();
        var jsonpMapper = client._transport().jsonpMapper();

        try (var generator = jsonpMapper.jsonProvider()
                .createGenerator(writer)) {
            mapping.serialize(generator, jsonpMapper);
        }

        return writer.toString();
    }

    private void attachAlias() throws IOException {
        // Đọc lại để tránh đổi alias đã được tạo bởi instance khác.
        boolean aliasExists = client.indices()
                .existsAlias(request -> request.name(ALIAS))
                .value();

        if (aliasExists) {
            validateWriteAlias();
            return;
        }

        var response = client.indices().updateAliases(request -> request
                .actions(action -> action
                        .add(add -> add
                                .index(INITIAL_INDEX)
                                .alias(ALIAS)
                                .isWriteIndex(true)
                        )
                )
        );

        if (!response.acknowledged()) {
            throw new IllegalStateException(
                    "Chưa nhận được xác nhận gắn alias " + ALIAS
                            + ". Cần kiểm tra lại trạng thái Elasticsearch."
            );
        }
    }

    private void validateWriteAlias() throws IOException {
        var response = client.indices()
                .getAlias(request -> request.name(ALIAS));

        var indices = response.result();

        long writeIndexCount = indices.values().stream()
                .map(index -> index.aliases().get(ALIAS))
                .filter(metadata -> metadata != null)
                .filter(metadata ->
                        Boolean.TRUE.equals(metadata.isWriteIndex()))
                .count();

        if (writeIndexCount == 1) {
            return;
        }

        // Một index duy nhất và không khai báo is_write_index:
        // Elasticsearch mặc định cho phép ghi vào index đó.
        if (indices.size() == 1) {
            var metadata = indices.values().iterator().next()
                    .aliases().get(ALIAS);

            if (metadata != null && metadata.isWriteIndex() == null) {
                return;
            }
        }

        throw new IllegalStateException(
                "Alias " + ALIAS
                        + " không có đích ghi hợp lệ. Các index: "
                        + indices.keySet()
        );
    }
}
