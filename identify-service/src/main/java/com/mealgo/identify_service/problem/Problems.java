package com.mealgo.identify_service.problem;

import com.mealgo.identify_service.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.util.Map;
import java.util.Set;

public final class Problems {

	private static final Set<String> RESERVED_PROPERTIES = Set.of("type", "title", "status", "detail", "instance", "code");

	private Problems() {
	}

	public static ProblemDetail of(ErrorCode errorCode, String detail, ProblemProperties properties, HttpServletRequest request) {
		return of(errorCode, detail, properties, request, Map.of());
	}

	public static ProblemDetail of(ErrorCode errorCode, String detail, ProblemProperties properties,
			HttpServletRequest request, Map<String, Object> extensions) {
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
