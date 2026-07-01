package com.zuvomo.project.assessment.entity;

public enum SignalStatus {
	OPEN,
	TARGET_HIT,
	STOPLOSS_HIT,
	EXPIRED;

	public boolean isFinal() {
		return this != OPEN;
	}
}
