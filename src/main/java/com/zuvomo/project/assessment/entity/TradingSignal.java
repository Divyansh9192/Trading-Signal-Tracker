package com.zuvomo.project.assessment.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import lombok.*;
import org.hibernate.annotations.Check;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "trading_signals", indexes = {
		@Index(name = "idx_trading_signals_symbol", columnList = "symbol"),
		@Index(name = "idx_trading_signals_status", columnList = "status"),
		@Index(name = "idx_trading_signals_expiry_time", columnList = "expiry_time")
})
@Check(constraints = "entry_price > 0 AND stop_loss > 0 AND target_price > 0")
@Getter
@Setter
@NoArgsConstructor
public class TradingSignal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 20)
	private String symbol;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private Direction direction;

	@Column(name = "entry_price", nullable = false, precision = 28, scale = 8)
	private BigDecimal entryPrice;

	@Column(name = "stop_loss", nullable = false, precision = 28, scale = 8)
	private BigDecimal stopLoss;

	@Column(name = "target_price", nullable = false, precision = 28, scale = 8)
	private BigDecimal targetPrice;

	@Column(name = "entry_time", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private OffsetDateTime entryTime;

	@Column(name = "expiry_time", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private OffsetDateTime expiryTime;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private OffsetDateTime createdAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private SignalStatus status = SignalStatus.OPEN;

	@Column(name = "realized_roi", precision = 10, scale = 2)
	private BigDecimal realizedRoi;


	public TradingSignal(String symbol, Direction direction, BigDecimal entryPrice, BigDecimal stopLoss,
			BigDecimal targetPrice, OffsetDateTime entryTime, OffsetDateTime expiryTime) {
		this.symbol = symbol;
		this.direction = direction;
		this.entryPrice = entryPrice;
		this.stopLoss = stopLoss;
		this.targetPrice = targetPrice;
		this.entryTime = entryTime;
		this.expiryTime = expiryTime;
		this.status = SignalStatus.OPEN;
	}

	@PrePersist
	void prePersist() {
		if (status == null) {
			status = SignalStatus.OPEN;
		}
	}
}
