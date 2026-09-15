package io.apikit.apicommon.problem;

import io.apikit.apicommon.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * Stateless factory for RFC 9457 {@link ProblemDetail} response bodies.
 *
 * <p>The factory combines an {@link ErrorCode}, detail message, configured
 * problem type URI and optional request information. Extension properties are
 * supported for metadata, but standard Problem Detail fields and the shared
 * {@code code} property are protected from being overwritten.</p>
 *
 * @author phongtm
 */
public final class Problems {

	private static final Set<String> RESERVED_PROPERTIES = Set.of(
			"type", "title", "status", "detail", "instance", "code");

	private Problems() {
	}

	public static ProblemDetail of(
			ErrorCode errorCode,
			String detail,
			ProblemProperties properties,
			HttpServletRequest request) {
		return of(errorCode, detail, properties, request, Map.of());
	}

	public static ProblemDetail of(
			ErrorCode errorCode,
			String detail,
			ProblemProperties properties,
			HttpServletRequest request,
			Map<String, Object> extensions) {
		ProblemProperties props = properties != null ? properties : new ProblemProperties();
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(errorCode.status(), detail);
		problem.setTitle(errorCode.title());
		problem.setType(props.typeUri(errorCode.typeSuffix()));
		problem.setProperty("code", errorCode.code());
		if (request != null) {
			problem.setInstance(URI.create(request.getRequestURI()));
		}
		if (extensions != null && !extensions.isEmpty()) {
			extensions.forEach((name, value) -> {
				if (RESERVED_PROPERTIES.contains(name)) {
					throw new IllegalArgumentException("Reserved problem property: " + name);
				}
				problem.setProperty(name, value);
			});
		}
		return problem;
	}
}
