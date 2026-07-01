package com.zuvomo.project.assessment.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zuvomo.project.assessment.entity.Direction;
import com.zuvomo.project.assessment.entity.SignalStatus;
import com.zuvomo.project.assessment.entity.TradingSignal;
import com.zuvomo.project.assessment.dto.CreateSignalRequest;
import com.zuvomo.project.assessment.dto.SignalResponse;
import com.zuvomo.project.assessment.dto.SignalStatusResponse;
import com.zuvomo.project.assessment.exception.ExternalPriceException;
import com.zuvomo.project.assessment.exception.SignalNotFoundException;
import com.zuvomo.project.assessment.market.MarketPriceClient;
import com.zuvomo.project.assessment.repository.TradingSignalRepository;

@Service
@RequiredArgsConstructor
public class SignalService {

	private final TradingSignalRepository repository;
	private final SignalValidator validator;
	private final MarketPriceClient marketPriceClient;
	private final RoiCalculator roiCalculator;
	private final Clock clock;

	@Transactional
	public SignalResponse createSignal(CreateSignalRequest request) {
		validator.validate(request);

		TradingSignal signal = new TradingSignal(
				request.symbol().trim().toUpperCase(),
				request.direction(),
				request.entryPrice(),
				request.stopLoss(),
				request.targetPrice(),
				request.entryTime(),
				request.expiryTime());

		return SignalResponse.from(repository.save(signal));
	}

	@Transactional
	public List<SignalResponse> getSignals() {
		return repository.findAll().stream()
				.map(this::evaluateIfNeeded)
				.map(SignalEvaluation::signal)
				.map(SignalResponse::from)
				.toList();
	}

	@Transactional
	public SignalResponse getSignal(Long id) {
		return SignalResponse.from(evaluateIfNeeded(findSignal(id)).signal());
	}

	@Transactional
	public SignalStatusResponse getSignalStatus(Long id) {
		SignalEvaluation evaluation = evaluateIfNeeded(findSignal(id));
		TradingSignal signal = evaluation.signal();
		return new SignalStatusResponse(
				signal.getId(),
				signal.getSymbol(),
				signal.getStatus(),
				evaluation.currentPrice(),
				evaluation.currentRoi(),
				signal.getRealizedRoi(),
				evaluation.evaluatedAt());
	}

	@Transactional
	public void deleteSignal(Long id) {
		if (!repository.existsById(id)) {
			throw new SignalNotFoundException(id);
		}
		repository.deleteById(id);
	}

	@Scheduled(fixedDelayString = "${signals.evaluation.fixed-delay-ms:60000}")
	@Transactional
	public void evaluateOpenSignals() {
		repository.findByStatus(SignalStatus.OPEN).forEach(signal -> {
			try {
				evaluateIfNeeded(signal);
			}
			catch (ExternalPriceException ignored) {
				// A transient Binance error must not break future scheduled evaluations.
			}
		});
	}

	private TradingSignal findSignal(Long id) {
		return repository.findById(id).orElseThrow(() -> new SignalNotFoundException(id));
	}

	private SignalEvaluation evaluateIfNeeded(TradingSignal signal) {
		OffsetDateTime now = OffsetDateTime.now(clock);

		if (signal.getStatus().isFinal()) {
			return new SignalEvaluation(signal, null, signal.getRealizedRoi(), now);
		}

		if (now.isAfter(signal.getExpiryTime())) {
			signal.setStatus(SignalStatus.EXPIRED);
			return new SignalEvaluation(repository.save(signal), null, null, now);
		}

		BigDecimal currentPrice = marketPriceClient.getCurrentPrice(signal.getSymbol());
		BigDecimal currentRoi = roiCalculator.calculate(signal.getDirection(), signal.getEntryPrice(), currentPrice);
		SignalStatus nextStatus = determineStatus(signal, currentPrice);

		if (nextStatus.isFinal()) {
			signal.setStatus(nextStatus);
			signal.setRealizedRoi(currentRoi);
			return new SignalEvaluation(repository.save(signal), currentPrice, currentRoi, now);
		}

		return new SignalEvaluation(signal, currentPrice, currentRoi, now);
	}

	private SignalStatus determineStatus(TradingSignal signal, BigDecimal currentPrice) {
		if (signal.getDirection() == Direction.BUY) {
			if (currentPrice.compareTo(signal.getTargetPrice()) >= 0) {
				return SignalStatus.TARGET_HIT;
			}
			if (currentPrice.compareTo(signal.getStopLoss()) <= 0) {
				return SignalStatus.STOPLOSS_HIT;
			}
			return SignalStatus.OPEN;
		}

		if (currentPrice.compareTo(signal.getTargetPrice()) <= 0) {
			return SignalStatus.TARGET_HIT;
		}
		if (currentPrice.compareTo(signal.getStopLoss()) >= 0) {
			return SignalStatus.STOPLOSS_HIT;
		}
		return SignalStatus.OPEN;
	}
}
