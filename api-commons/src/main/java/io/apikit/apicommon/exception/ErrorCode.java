package io.apikit.apicommon.exception;

import org.springframework.http.HttpStatus;

import java.util.Locale;

/**
 * Contract for a machine-readable API error definition.
 *
 * <p>The code is stable and intended for client logic, while the status and
 * title describe the HTTP/RFC 9457 response. Each consuming service can
 * implement this interface with its own business error catalog.</p>
 *
 * @author phongtm
 */
public interface ErrorCode {

	/**
	 * JSON extension {@code code}, e.g. {@code EMAIL_ALREADY_EXISTS}.
	 */
	String code();

	HttpStatus status();

	/**
	 * Suffix for {@code type} URI: {@code EMAIL_ALREADY_EXISTS} → {@code email-already-exists}.
	 */
	default String typeSuffix() {
		return code().toLowerCase(Locale.ROOT).replace('_', '-');
	}

	/**
	 * RFC 9457 {@code title}; defaults to HTTP reason phrase.
	 */
	default String title() {
		return status().getReasonPhrase();
	}
}
