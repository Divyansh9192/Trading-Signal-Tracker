package com.zuvomo.project.assessment.exception;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.http.HttpStatus;

public record ErrorResponse(
		OffsetDateTime timestamp,
		int status,
		String error,
		String message,
		List<String> details) {

	public static ErrorResponse of(HttpStatus status, String message, List<String> details) {
		return new ErrorResponse(OffsetDateTime.now(ZoneOffset.UTC), status.value(), status.getReasonPhrase(), message,
				details);
	}
}
