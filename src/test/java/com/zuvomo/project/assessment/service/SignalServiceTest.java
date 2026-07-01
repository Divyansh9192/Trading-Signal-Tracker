package com.zuvomo.project.assessment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.zuvomo.project.assessment.entity.Direction;
import com.zuvomo.project.assessment.entity.SignalStatus;
import com.zuvomo.project.assessment.entity.TradingSignal;
import com.zuvomo.project.assessment.dto.SignalStatusResponse;
import com.zuvomo.project.assessment.market.MarketPriceClient;
import com.zuvomo.project.assessment.repository.TradingSignalRepository;

class SignalServiceTest {

	private final Clock clock = Clock.fixed(Instant.parse("2026-07-01T12:00:00Z"), ZoneOffset.UTC);

	private TradingSignalRepository repository;
	private MarketPriceClient marketPriceClient;
	private SignalService service;

	@BeforeEach
	void setUp() {
		repository = mock(TradingSignalRepository.class);
		marketPriceClient = mock(MarketPriceClient.class);
		service = new SignalService(repository, new SignalValidator(clock), marketPriceClient, new RoiCalculator(), clock);
	}

	@Test
	void marksBuyTargetHitAndStoresRealizedRoi() {
		TradingSignal signal = signal(Direction.BUY, "100", "90", "110", SignalStatus.OPEN);
		when(repository.findById(1L)).thenReturn(Optional.of(signal));
		when(repository.save(signal)).thenReturn(signal);
		when(marketPriceClient.getCurrentPrice("BTCUSDT")).thenReturn(new BigDecimal("111"));

		SignalStatusResponse response = service.getSignalStatus(1L);

		assertThat(response.status()).isEqualTo(SignalStatus.TARGET_HIT);
		assertThat(response.currentPrice()).isEqualByComparingTo("111");
		assertThat(response.realizedRoi()).isEqualByComparingTo("11.00");
		assertThat(signal.getStatus()).isEqualTo(SignalStatus.TARGET_HIT);
		verify(repository).save(signal);
	}

	@Test
	void marksBuyStopLossHitAndStoresRealizedRoi() {
		TradingSignal signal = signal(Direction.BUY, "100", "90", "110", SignalStatus.OPEN);
		when(repository.findById(1L)).thenReturn(Optional.of(signal));
		when(repository.save(signal)).thenReturn(signal);
		when(marketPriceClient.getCurrentPrice("BTCUSDT")).thenReturn(new BigDecimal("89.50"));

		SignalStatusResponse response = service.getSignalStatus(1L);

		assertThat(response.status()).isEqualTo(SignalStatus.STOPLOSS_HIT);
		assertThat(response.realizedRoi()).isEqualByComparingTo("-10.50");
		verify(repository).save(signal);
	}

	@Test
	void marksExpiredBeforeFetchingPrice() {
		TradingSignal signal = signal(Direction.BUY, "100", "90", "110", SignalStatus.OPEN);
		signal = new TradingSignal("BTCUSDT", Direction.BUY, new BigDecimal("100"), new BigDecimal("90"),
				new BigDecimal("110"), now().minusHours(25), now().minusMinutes(1));
		signal.setId(1L);
		when(repository.findById(1L)).thenReturn(Optional.of(signal));
		when(repository.save(signal)).thenReturn(signal);

		SignalStatusResponse response = service.getSignalStatus(1L);

		assertThat(response.status()).isEqualTo(SignalStatus.EXPIRED);
		assertThat(response.currentPrice()).isNull();
		assertThat(response.realizedRoi()).isNull();
		verifyNoInteractions(marketPriceClient);
		verify(repository).save(signal);
	}

	@Test
	void doesNotTransitionFinalTargetHitSignal() {
		TradingSignal signal = signal(Direction.BUY, "100", "90", "110", SignalStatus.TARGET_HIT);
		signal.setRealizedRoi(new BigDecimal("10.00"));
		when(repository.findById(1L)).thenReturn(Optional.of(signal));

		SignalStatusResponse response = service.getSignalStatus(1L);

		assertThat(response.status()).isEqualTo(SignalStatus.TARGET_HIT);
		assertThat(response.realizedRoi()).isEqualByComparingTo("10.00");
		verifyNoInteractions(marketPriceClient);
		verify(repository, never()).save(signal);
	}

	@Test
	void doesNotTransitionExpiredSignal() {
		TradingSignal signal = signal(Direction.SELL, "100", "110", "90", SignalStatus.EXPIRED);
		when(repository.findById(1L)).thenReturn(Optional.of(signal));

		SignalStatusResponse response = service.getSignalStatus(1L);

		assertThat(response.status()).isEqualTo(SignalStatus.EXPIRED);
		verifyNoInteractions(marketPriceClient);
		verify(repository, never()).save(signal);
	}

	private TradingSignal signal(Direction direction, String entry, String stopLoss, String target, SignalStatus status) {
		TradingSignal signal = new TradingSignal("BTCUSDT", direction, new BigDecimal(entry), new BigDecimal(stopLoss),
				new BigDecimal(target), now().minusHours(1), now().plusHours(1));
		signal.setId(1L);
		signal.setCreatedAt(now().minusHours(1));
		signal.setStatus(status);
		return signal;
	}

	private OffsetDateTime now() {
		return OffsetDateTime.now(clock);
	}
}
