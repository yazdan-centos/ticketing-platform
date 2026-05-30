package com.mapnaom.ticketingplatform.controller;

import com.mapnaom.ticketingplatform.dto.SlaContractRequestDto;
import com.mapnaom.ticketingplatform.dto.SlaContractResponseDto;
import com.mapnaom.ticketingplatform.service.SlaContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin

@RestController
@RequestMapping("/api/sla-contracts")
@RequiredArgsConstructor
public class SlaContractController {

    private final SlaContractService slaContractService;

    // --- Create SLA Contract ---
    @PostMapping
    public ResponseEntity<SlaContractResponseDto> createSlaContract(@Valid @RequestBody SlaContractRequestDto dto) {
        SlaContractResponseDto createdContract = slaContractService.createSlaContract(dto);
        return new ResponseEntity<>(createdContract, HttpStatus.CREATED);
    }

    // --- Get All SLA Contracts ---
    @GetMapping
    public ResponseEntity<List<SlaContractResponseDto>> getAllSlaContracts() {
        List<SlaContractResponseDto> contracts = slaContractService.getAllSlaContracts();
        return ResponseEntity.ok(contracts);
    }

    // --- Get SLA Contract By ID ---
    @GetMapping("/{id}")
    public ResponseEntity<SlaContractResponseDto> getSlaContractById(@PathVariable Long id) {
        SlaContractResponseDto contract = slaContractService.getSlaContractById(id);
        return ResponseEntity.ok(contract);
    }

    // --- Update SLA Contract ---
    @PutMapping("/{id}")
    public ResponseEntity<SlaContractResponseDto> updateSlaContract(
            @PathVariable Long id,
            @Valid @RequestBody SlaContractRequestDto dto) {
        SlaContractResponseDto updatedContract = slaContractService.updateSlaContract(id, dto);
        return ResponseEntity.ok(updatedContract);
    }

    // --- Delete SLA Contract ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSlaContract(@PathVariable Long id) {
        slaContractService.deleteSlaContract(id);
        return ResponseEntity.noContent().build();
    }
}