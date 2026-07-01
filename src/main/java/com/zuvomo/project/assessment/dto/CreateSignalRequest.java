package com.zuvomo.project.assessment.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.zuvomo.project.assessment.entity.Direction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateSignalRequest(
		@NotBlank
		@Size(max = 20)
		@Pattern(regexp = "^[A-Za-z0-9]+$", message = "symbol must contain only letters and numbers")
		String symbol,

		@NotNull
		Direction direction,

		@NotNull
		@DecimalMin(value = "0.0", inclusive = false)
		@Digits(integer = 20, fraction = 8)
		BigDecimal entryPrice,

		@NotNull
		@DecimalMin(value = "0.0", inclusive = false)
		@Digits(integer = 20, fraction = 8)
		BigDecimal stopLoss,

		@NotNull
		@DecimalMin(value = "0.0", inclusive = false)
		@Digits(integer = 20, fraction = 8)
		BigDecimal targetPrice,

		@NotNull
		OffsetDateTime entryTime,

		@NotNull
		OffsetDateTime expiryTime) {
}
