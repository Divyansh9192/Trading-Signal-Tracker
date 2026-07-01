package com.zuvomo.project.assessment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.zuvomo.project.assessment.entity.Direction;
import com.zuvomo.project.assessment.dto.CreateSignalRequest;
import com.zuvomo.project.assessment.exception.SignalValidationException;

class SignalValidatorTest {

	private final Clock clock = Clock.fixed(Instant.parse("2026-07-01T12:00:00Z"), ZoneOffset.UTC);

	private SignalValidator validator;

	@BeforeEach
	void setUp() {
		validator = new SignalValidator(clock);
	}

	@Test
	void acceptsValidBuySignal() {
		assertThatCode(() -> validator.validate(request(Direction.BUY, "100", "90", "120", now().minusHours(24),
				now().plusHours(1)))).doesNotThrowAnyException();
	}

	@Test
	void rejectsInvalidBuyPriceRelationship() {
		assertThatThrownBy(() -> validator.validate(request(Direction.BUY, "100", "101", "99", now(), now().plusHours(1))))
				.isInstanceOf(SignalValidationException.class)
				.hasMessage("Signal request failed validation")
				.satisfies(ex -> assertThat(((SignalValidationException) ex).getErrors()).contains(
						"BUY signal requires stopLoss to be less than entryPrice",
						"BUY signal requires targetPrice to be greater than entryPrice"));
	}

	@Test
	void acceptsValidSellSignal() {
		assertThatCode(() -> validator.validate(request(Direction.SELL, "100", "110", "80", now().minusHours(1),
				now().plusHours(1)))).doesNotThrowAnyException();
	}

	@Test
	void rejectsInvalidSellPriceRelationship() {
		assertThatThrownBy(() -> validator.validate(request(Direction.SELL, "100", "99", "101", now(), now().plusHours(1))))
				.isInstanceOf(SignalValidationException.class)
				.satisfies(ex -> assertThat(((SignalValidationException) ex).getErrors()).contains(
						"SELL signal requires stopLoss to be greater than entryPrice",
						"SELL signal requires targetPrice to be less than entryPrice"));
	}

	@Test
	void rejectsInvalidTimeRules() {
		assertThatThrownBy(() -> validator.validate(request(Direction.BUY, "100", "90", "120", now().minusHours(25),
				now().minusHours(26))))
				.isInstanceOf(SignalValidationException.class)
				.satisfies(ex -> assertThat(((SignalValidationException) ex).getErrors()).contains(
						"expiryTime must be after entryTime",
						"entryTime may be at most 24 hours in the past"));
	}

	private CreateSignalRequest request(Direction direction, String entry, String stopLoss, String target,
			OffsetDateTime entryTime, OffsetDateTime expiryTime) {
		return new CreateSignalRequest("BTCUSDT", direction, new BigDecimal(entry), new BigDecimal(stopLoss),
				new BigDecimal(target), entryTime, expiryTime);
	}

	private OffsetDateTime now() {
		return OffsetDateTime.now(clock);
	}
}
