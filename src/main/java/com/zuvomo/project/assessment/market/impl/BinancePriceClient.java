package com.zuvomo.project.assessment.market.impl;

import java.math.BigDecimal;

import com.zuvomo.project.assessment.market.MarketPriceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.zuvomo.project.assessment.exception.ExternalPriceException;

@Component
public class BinancePriceClient implements MarketPriceClient {

	private static final String TICKER_PRICE_ENDPOINT = "/api/v3/ticker/price";

	private final RestClient restClient;

	public BinancePriceClient(RestClient.Builder restClientBuilder,
			@Value("${binance.base-url:https://api.binance.com}") String baseUrl) {
		this.restClient = restClientBuilder.baseUrl(baseUrl).build();
	}

	@Override
	public BigDecimal getCurrentPrice(String symbol) {
		try {
			BinanceTickerPrice response = restClient.get()
					.uri(uriBuilder -> uriBuilder.path(TICKER_PRICE_ENDPOINT).queryParam("symbol", symbol).build())
					.retrieve()
					.body(BinanceTickerPrice.class);

			if (response == null || response.price() == null || response.price().isBlank()) {
				throw new ExternalPriceException("Binance returned an empty price for " + symbol);
			}

			return new BigDecimal(response.price());
		}
		catch (RestClientException | NumberFormatException ex) {
			throw new ExternalPriceException("Unable to fetch Binance price for " + symbol, ex);
		}
	}

	private record BinanceTickerPrice(String symbol, String price) {
	}
}
