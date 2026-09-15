package io.apikit.apicommon.problem;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import java.net.URI;

/**
 * External configuration for RFC 9457 problem {@code type} URIs.
 *
 * <p>Spring Boot binds this class from {@code apikit.problem.*}. The base URI
 * is combined with an {@link io.apikit.apicommon.exception.ErrorCode} suffix,
 * for example {@code https://api.example.com/problems/not-found}.</p>
 *
 * @author phongtm
 */
@ConfigurationProperties(prefix = "apikit.problem")
@Validated
public class ProblemProperties {

	/**
	 * Base for RFC 9457 {@code type} URIs, e.g. {@code https://apikit.io/problems}.
	 */
	@NotBlank
	private String typeBaseUri = "https://apikit.io/problems";

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
		String base = typeBaseUri.endsWith("/")
				? typeBaseUri.substring(0, typeBaseUri.length() - 1)
				: typeBaseUri;
		URI baseUri = URI.create(base);
		if (!baseUri.isAbsolute() || baseUri.getQuery() != null || baseUri.getFragment() != null) {
			throw new IllegalArgumentException("typeBaseUri must be an absolute URI without query or fragment");
		}
		return URI.create(base + "/" + typeSuffix);
	}
}
