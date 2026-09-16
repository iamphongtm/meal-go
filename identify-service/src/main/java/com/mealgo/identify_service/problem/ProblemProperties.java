package com.mealgo.identify_service.problem;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import java.net.URI;

@ConfigurationProperties(prefix = "mealgo.problem")
@Validated
public class ProblemProperties {

	@NotBlank
	private String typeBaseUri = "https://mealgo.com/problems";

	public String getTypeBaseUri() {
		return typeBaseUri;
	}

	public void setTypeBaseUri(String typeBaseUri) {
		this.typeBaseUri = typeBaseUri;
	}

	public URI typeUri(String typeSuffix) {
		if (typeSuffix == null || !typeSuffix.matches("[a-z0-9]+(?:-[a-z0-9]+)*")) {
			throw new IllegalArgumentException("typeSuffix must contain lowercase letters, digits and hyphens");
		}
		if (typeBaseUri == null || typeBaseUri.isBlank()) {
			throw new IllegalArgumentException("typeBaseUri must not be blank");
		}
		String base = typeBaseUri.endsWith("/") ? typeBaseUri.substring(0, typeBaseUri.length() - 1) : typeBaseUri;
		URI baseUri = URI.create(base);
		if (!baseUri.isAbsolute() || baseUri.getQuery() != null || baseUri.getFragment() != null) {
			throw new IllegalArgumentException("typeBaseUri must be an absolute URI without query or fragment");
		}
		return URI.create(base + "/" + typeSuffix);
	}
}
