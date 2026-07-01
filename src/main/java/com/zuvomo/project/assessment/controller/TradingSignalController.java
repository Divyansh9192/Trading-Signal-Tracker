package com.zuvomo.project.assessment.controller;

import java.net.URI;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zuvomo.project.assessment.dto.CreateSignalRequest;
import com.zuvomo.project.assessment.dto.SignalResponse;
import com.zuvomo.project.assessment.dto.SignalStatusResponse;
import com.zuvomo.project.assessment.service.SignalService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/signals")
@RequiredArgsConstructor
@Slf4j
public class TradingSignalController {

	private final SignalService signalService;

	@PostMapping
	public ResponseEntity<SignalResponse> createSignal(@Valid @RequestBody CreateSignalRequest request) {
		SignalResponse response = signalService.createSignal(request);
		return ResponseEntity.created(URI.create("/api/signals/" + response.id())).body(response);
	}

	@GetMapping
	public List<SignalResponse> getSignals() {
		return signalService.getSignals();
	}

	@GetMapping("/{id}")
	public SignalResponse getSignal(@PathVariable Long id) {
		return signalService.getSignal(id);
	}

	@GetMapping("/{id}/status")
	public SignalStatusResponse getSignalStatus(@PathVariable Long id) {
		return signalService.getSignalStatus(id);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteSignal(@PathVariable Long id) {
		signalService.deleteSignal(id);
		return ResponseEntity.noContent().build();
	}
}
