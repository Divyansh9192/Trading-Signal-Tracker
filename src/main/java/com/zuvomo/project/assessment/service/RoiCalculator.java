package com.zuvomo.project.assessment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.zuvomo.project.assessment.entity.Direction;

@Component
public class RoiCalculator {

	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

	public BigDecimal calculate(Direction direction, BigDecimal entryPrice, BigDecimal currentPrice) {
		if (entryPrice == null || currentPrice == null || entryPrice.signum() <= 0) {
			throw new IllegalArgumentException("entryPrice must be positive and currentPrice must be present");
		}

		BigDecimal difference = direction == Direction.BUY
				? currentPrice.subtract(entryPrice)
				: entryPrice.subtract(currentPrice);

		return difference.multiply(ONE_HUNDRED).divide(entryPrice, 2, RoundingMode.HALF_UP);
	}
}
