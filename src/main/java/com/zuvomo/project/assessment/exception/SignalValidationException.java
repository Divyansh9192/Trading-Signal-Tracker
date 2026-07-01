package com.zuvomo.project.assessment.exception;

import java.util.List;

public class SignalValidationException extends RuntimeException {

	private final List<String> errors;

	public SignalValidationException(List<String> errors) {
		super("Signal request failed validation");
		this.errors = List.copyOf(errors);
	}

	public List<String> getErrors() {
		return errors;
	}
}
