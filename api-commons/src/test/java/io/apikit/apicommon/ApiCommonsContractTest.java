package io.apikit.apicommon;

import io.apikit.apicommon.dto.CursorPageResponse;
import io.apikit.apicommon.dto.PageResponse;
import io.apikit.apicommon.exception.CommonErrorCode;
import io.apikit.apicommon.problem.ProblemProperties;
import io.apikit.apicommon.problem.Problems;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApiCommonsContractTest {

	@Test
	void problemDetailsHaveStableContractAndRejectReservedExtensions() {
		ProblemProperties properties = new ProblemProperties();
		var problem = Problems.of(
				CommonErrorCode.BAD_REQUEST,
				"Invalid request",
				properties,
				null,
				Map.of("traceId", "abc-123"));

		assertEquals("BAD_REQUEST", problem.getProperties().get("code"));
		assertEquals("https://apikit.io/problems/bad-request", problem.getType().toString());
		assertEquals("abc-123", problem.getProperties().get("traceId"));
		assertThrows(IllegalArgumentException.class, () -> Problems.of(
				CommonErrorCode.BAD_REQUEST,
				"Invalid request",
				properties,
				null,
				Map.of("code", "OVERRIDE")));
	}

	@Test
	void typeBaseUriAndSuffixAreValidated() {
		ProblemProperties properties = new ProblemProperties();
		properties.setTypeBaseUri("https://example.test/problems/");
		assertEquals("https://example.test/problems/bad-request",
				properties.typeUri("bad-request").toString());
		assertThrows(IllegalArgumentException.class, () -> properties.typeUri("Bad Request"));
		assertThrows(IllegalArgumentException.class, () -> properties.typeUri("bad/request"));
	}

	@Test
	void pageResponseCopiesItems() {
		List<String> source = new ArrayList<>(List.of("one"));
		PageResponse<String> response = new PageResponse<>(source, 0, 10, 1, 1);
		source.add("two");

		assertEquals(List.of("one"), response.items());
	}

	@Test
	void cursorResponseRequiresConsistentCursorState() {
		assertEquals(List.of("one"), CursorPageResponse.of(List.of("one"), "next", true).items());
		assertThrows(IllegalArgumentException.class,
				() -> new CursorPageResponse<>(List.of(), null, true));
		assertThrows(IllegalArgumentException.class,
				() -> new CursorPageResponse<>(List.of(), "next", false));
	}

	@Test
	void customProblemPropertiesAreNotGlobal() {
		ProblemProperties first = new ProblemProperties();
		first.setTypeBaseUri("https://first.test/problems");
		ProblemProperties second = new ProblemProperties();
		second.setTypeBaseUri("https://second.test/problems");

		assertEquals("https://first.test/problems/bad-request",
				Problems.of(CommonErrorCode.BAD_REQUEST, "bad", first, null).getType().toString());
		assertEquals("https://second.test/problems/bad-request",
				Problems.of(CommonErrorCode.BAD_REQUEST, "bad", second, null).getType().toString());
	}
}
