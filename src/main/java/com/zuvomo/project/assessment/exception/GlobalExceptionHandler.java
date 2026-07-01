package com.zuvomo.project.assessment.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ErrorResponse> handleBeanValidation(MethodArgumentNotValidException ex) {
		List<String> details = ex.getBindingResult().getFieldErrors().stream()
				.map(this::formatFieldError)
				.toList();
		return error(HttpStatus.BAD_REQUEST, "Request validation failed", details);
	}

	@ExceptionHandler(SignalValidationException.class)
	ResponseEntity<ErrorResponse> handleSignalValidation(SignalValidationException ex) {
		return error(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getErrors());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException ex) {
		return error(HttpStatus.BAD_REQUEST, "Request body is malformed or contains invalid enum/date values",
				List.of(ex.getMostSpecificCause().getMessage()));
	}

	@ExceptionHandler(SignalNotFoundException.class)
	ResponseEntity<ErrorResponse> handleNotFound(SignalNotFoundException ex) {
		return error(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
	}

	@ExceptionHandler(ExternalPriceException.class)
	ResponseEntity<ErrorResponse> handleExternalPrice(ExternalPriceException ex) {
		return error(HttpStatus.BAD_GATEWAY, ex.getMessage(), List.of());
	}

	private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, List<String> details) {
		return ResponseEntity.status(status).body(ErrorResponse.of(status, message, details));
	}

	private String formatFieldError(FieldError error) {
		return error.getField() + ": " + error.getDefaultMessage();
	}
}
