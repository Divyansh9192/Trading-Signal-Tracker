package com.zuvomo.project.assessment.market;

import java.math.BigDecimal;

public interface MarketPriceClient {

	BigDecimal getCurrentPrice(String symbol);
}
