package com.mealgo.identify_service.security;

import io.apikit.apicommon.exception.CommonErrorCode;
import io.apikit.apicommon.problem.ProblemProperties;
import io.apikit.apicommon.problem.Problems;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

public class ProblemDetailsAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;
	private final ProblemProperties problemProperties;

	public ProblemDetailsAccessDeniedHandler(
			ObjectMapper objectMapper,
			ProblemProperties problemProperties) {
		this.objectMapper = objectMapper;
		this.problemProperties = problemProperties;
	}

	@Override
	public void handle(
			HttpServletRequest request,
			HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		String detail = accessDeniedException.getMessage() != null
				? accessDeniedException.getMessage()
				: "Access is denied";
		ProblemDetail problem = Problems.of(
				CommonErrorCode.FORBIDDEN,
				detail,
				problemProperties,
				request);

		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), problem);
	}
}
