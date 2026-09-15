package com.mealgo.identify_service.security;

import io.apikit.apicommon.exception.CommonErrorCode;
import io.apikit.apicommon.problem.ProblemProperties;
import io.apikit.apicommon.problem.Problems;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

public class ProblemDetailsAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;
	private final ProblemProperties problemProperties;

	public ProblemDetailsAuthenticationEntryPoint(
			ObjectMapper objectMapper,
			ProblemProperties problemProperties) {
		this.objectMapper = objectMapper;
		this.problemProperties = problemProperties;
	}

	@Override
	public void commence(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		String detail = authException.getMessage() != null
				? authException.getMessage()
				: "Authentication is required";
		ProblemDetail problem = Problems.of(
				CommonErrorCode.UNAUTHORIZED,
				detail,
				problemProperties,
				request);

		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), problem);
	}
}
