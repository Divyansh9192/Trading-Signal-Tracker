package com.zuvomo.project.assessment.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.zuvomo.project.assessment.entity.SignalStatus;

public record SignalStatusResponse(
		Long id,
		String symbol,
		SignalStatus status,
		BigDecimal currentPrice,
		BigDecimal currentRoi,
		BigDecimal realizedRoi,
		OffsetDateTime evaluatedAt) {
}
