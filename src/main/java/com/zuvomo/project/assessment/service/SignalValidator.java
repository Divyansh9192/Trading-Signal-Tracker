package com.zuvomo.project.assessment.service;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.zuvomo.project.assessment.entity.Direction;
import com.zuvomo.project.assessment.dto.CreateSignalRequest;
import com.zuvomo.project.assessment.exception.SignalValidationException;

@Component
public class SignalValidator {

	static final Duration MAX_ENTRY_AGE = Duration.ofHours(24);

	private final Clock clock;

	public SignalValidator(Clock clock) {
		this.clock = clock;
	}

	public void validate(CreateSignalRequest request) {
		List<String> errors = new ArrayList<>();

		validatePrices(request, errors);
		validateTimes(request, errors);

		if (!errors.isEmpty()) {
			throw new SignalValidationException(errors);
		}
	}

	private void validatePrices(CreateSignalRequest request, List<String> errors) {
		if (request.direction() == null || request.entryPrice() == null || request.stopLoss() == null
				|| request.targetPrice() == null) {
			return;
		}

		if (request.direction() == Direction.BUY) {
			if (request.stopLoss().compareTo(request.entryPrice()) >= 0) {
				errors.add("BUY signal requires stopLoss to be less than entryPrice");
			}
			if (request.targetPrice().compareTo(request.entryPrice()) <= 0) {
				errors.add("BUY signal requires targetPrice to be greater than entryPrice");
			}
			return;
		}

		if (request.stopLoss().compareTo(request.entryPrice()) <= 0) {
			errors.add("SELL signal requires stopLoss to be greater than entryPrice");
		}
		if (request.targetPrice().compareTo(request.entryPrice()) >= 0) {
			errors.add("SELL signal requires targetPrice to be less than entryPrice");
		}
	}

	private void validateTimes(CreateSignalRequest request, List<String> errors) {
		if (request.entryTime() != null && request.expiryTime() != null
				&& !request.expiryTime().isAfter(request.entryTime())) {
			errors.add("expiryTime must be after entryTime");
		}

		if (request.entryTime() != null) {
			OffsetDateTime oldestAllowedEntryTime = OffsetDateTime.now(clock).minus(MAX_ENTRY_AGE);
			if (request.entryTime().isBefore(oldestAllowedEntryTime)) {
				errors.add("entryTime may be at most 24 hours in the past");
			}
		}
	}
}
