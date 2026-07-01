package com.zuvomo.project.assessment.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.zuvomo.project.assessment.entity.TradingSignal;

record SignalEvaluation(
		TradingSignal signal,
		BigDecimal currentPrice,
		BigDecimal currentRoi,
		OffsetDateTime evaluatedAt) {
}
