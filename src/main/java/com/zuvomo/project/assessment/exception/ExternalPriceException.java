package com.zuvomo.project.assessment.exception;

public class ExternalPriceException extends RuntimeException {

	public ExternalPriceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExternalPriceException(String message) {
		super(message);
	}
}
