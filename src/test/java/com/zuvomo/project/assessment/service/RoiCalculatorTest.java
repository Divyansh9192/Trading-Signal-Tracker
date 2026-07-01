package com.zuvomo.project.assessment.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.zuvomo.project.assessment.entity.Direction;

class RoiCalculatorTest {

	private final RoiCalculator calculator = new RoiCalculator();

	@Test
	void calculatesBuyRoiWithTwoDecimals() {
		BigDecimal roi = calculator.calculate(Direction.BUY, new BigDecimal("100.00"), new BigDecimal("112.345"));

		assertThat(roi).isEqualByComparingTo("12.35");
		assertThat(roi.scale()).isEqualTo(2);
	}

	@Test
	void calculatesSellRoiWithTwoDecimals() {
		BigDecimal roi = calculator.calculate(Direction.SELL, new BigDecimal("100.00"), new BigDecimal("84.995"));

		assertThat(roi).isEqualByComparingTo("15.01");
		assertThat(roi.scale()).isEqualTo(2);
	}
}
