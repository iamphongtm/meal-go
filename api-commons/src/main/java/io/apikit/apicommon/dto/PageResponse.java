package io.apikit.apicommon.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Immutable offset-based page response for APIs that use page-number
 * pagination.
 *
 * <p>The response contains the current page, page size, total number of
 * elements and total number of pages. Use {@link #of(Page)} to adapt a Spring
 * Data {@link Page}, or {@link #of(Page, Function)} when mapping entities to
 * API response DTOs.</p>
 *
 * @author phongtm
 */
public record PageResponse<T>(
		List<T> items,
		int page,
		int size,
		long totalElements,
		int totalPages
) {
	public PageResponse {
		items = List.copyOf(items);
	}

	public static <T> PageResponse<T> of(Page<T> page) {
		return new PageResponse<>(
				page.getContent(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages());
	}

	public static <T, R> PageResponse<R> of(Page<T> page, Function<T, R> mapper) {
		List<R> items = page.getContent().stream().map(mapper).toList();
		return new PageResponse<>(
				items,
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages());
	}
}
