package com.zuvomo.project.assessment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zuvomo.project.assessment.entity.SignalStatus;
import com.zuvomo.project.assessment.entity.TradingSignal;

public interface TradingSignalRepository extends JpaRepository<TradingSignal, Long> {

	List<TradingSignal> findByStatus(SignalStatus status);
}
