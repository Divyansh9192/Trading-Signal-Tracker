package com.zuvomo.project.assessment.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.zuvomo.project.assessment.entity.Direction;
import com.zuvomo.project.assessment.entity.SignalStatus;
import com.zuvomo.project.assessment.entity.TradingSignal;

public record SignalResponse(
		Long id,
		String symbol,
		Direction direction,
		BigDecimal entryPrice,
		BigDecimal stopLoss,
		BigDecimal targetPrice,
		OffsetDateTime entryTime,
		OffsetDateTime expiryTime,
		OffsetDateTime createdAt,
		SignalStatus status,
		BigDecimal realizedRoi) {

	public static SignalResponse from(TradingSignal signal) {
		return new SignalResponse(
				signal.getId(),
				signal.getSymbol(),
				signal.getDirection(),
				signal.getEntryPrice(),
				signal.getStopLoss(),
				signal.getTargetPrice(),
				signal.getEntryTime(),
				signal.getExpiryTime(),
				signal.getCreatedAt(),
				signal.getStatus(),
				signal.getRealizedRoi());
	}
}
