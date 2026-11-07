package com.mealgo.searchservice.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealgo.searchservice.dto.ResolvedAddress;
import com.mealgo.searchservice.exception.ApplicationException;
import com.mealgo.searchservice.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

/** Backend-only provider client. Never log request URIs or exception messages. */
@Component
public class VietMapClient {
    private final String apiKey;
    private final String baseUrl;
    private final Duration readTimeout;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public VietMapClient(
            @Value("${vietmap.api-key:}") String apiKey,
            @Value("${vietmap.base-url:https://maps.vietmap.vn}") String baseUrl,
            @Value("${vietmap.connect-timeout:3s}") Duration connectTimeout,
            @Value("${vietmap.read-timeout:5s}") Duration readTimeout,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.readTimeout = readTimeout;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    public ResolvedAddress reverse(double latitude, double longitude) {
        if (apiKey.isBlank()) {
            throw new ApplicationException(ErrorCode.GEOCODING_NOT_CONFIGURED);
        }
        URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/api/reverse/v4")
                .queryParam("apikey", "{key}")
                .queryParam("lat", latitude)
                .queryParam("lng", longitude)
                .queryParam("display_type", 1)
                .encode().buildAndExpand(apiKey).toUri();
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(readTimeout).header("Accept", "application/json").GET().build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 423 || response.statusCode() == 429
                    || response.statusCode() == 503) {
                throw new ApplicationException(ErrorCode.GEOCODING_UNAVAILABLE);
            }
            if (response.statusCode() != 200) {
                throw new ApplicationException(ErrorCode.GEOCODING_PROVIDER_ERROR);
            }
            JsonNode results = objectMapper.readTree(response.body());
            if (results == null || !results.isArray()) {
                throw new ApplicationException(ErrorCode.GEOCODING_PROVIDER_ERROR);
            }
            if (results.isEmpty()) {
                throw new ApplicationException(ErrorCode.ADDRESS_NOT_FOUND);
            }
            JsonNode address = results.get(0);
            if (!address.isObject() || text(address, "display") == null) {
                throw new ApplicationException(ErrorCode.GEOCODING_PROVIDER_ERROR);
            }
            return new ResolvedAddress(latitude, longitude, text(address, "name"),
                    boundary(address, 2), boundary(address, 0), text(address, "display"));
        } catch (HttpTimeoutException exception) {
            throw new ApplicationException(ErrorCode.GEOCODING_TIMEOUT);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApplicationException(ErrorCode.GEOCODING_UNAVAILABLE);
        } catch (IOException exception) {
            throw new ApplicationException(ErrorCode.GEOCODING_PROVIDER_ERROR);
        }
    }

    private String boundary(JsonNode address, int type) {
        JsonNode boundaries = address.path("boundaries");
        if (!boundaries.isArray()) return null;
        for (JsonNode boundary : boundaries) {
            if (boundary.path("type").isIntegralNumber() && boundary.path("type").intValue() == type) {
                return text(boundary, "full_name");
            }
        }
        return null;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isTextual() && !value.textValue().isBlank() ? value.textValue() : null;
    }
}
