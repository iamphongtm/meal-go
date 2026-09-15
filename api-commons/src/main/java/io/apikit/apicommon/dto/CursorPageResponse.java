package io.apikit.apicommon.dto;

import java.util.List;
import java.util.function.Function;

/**
 * Immutable cursor-based page response for keyset or "load more"
 * pagination.
 *
 * <p>{@code nextCursor} is present exactly when {@code hasMore} is true.
 * This invariant prevents consumers from requesting a next page without a
 * cursor or from ignoring a cursor that was returned by the server.</p>
 *
 * @author phongtm
 */
public record CursorPageResponse<T>(
		List<T> items,
		String nextCursor,
		boolean hasMore
) {
	public CursorPageResponse {
		items = List.copyOf(items);
		if (hasMore != (nextCursor != null)) {
			throw new IllegalArgumentException("hasMore must match whether nextCursor is present");
		}
	}

	public static <T> CursorPageResponse<T> of(List<T> items, String nextCursor, boolean hasMore) {
		return new CursorPageResponse<>(items, nextCursor, hasMore);
	}

	public static <T, R> CursorPageResponse<R> of(
			List<T> items,
			String nextCursor,
			boolean hasMore,
			Function<T, R> mapper) {
		List<R> mapped = items.stream().map(mapper).toList();
		return new CursorPageResponse<>(mapped, nextCursor, hasMore);
	}
}
