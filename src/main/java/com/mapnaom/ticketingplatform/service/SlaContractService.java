package com.mapnaom.ticketingmanagerserver.service;

import com.mapnaom.ticketingmanagerserver.dto.SlaContractRequestDto;
import com.mapnaom.ticketingmanagerserver.dto.SlaContractResponseDto;
import com.mapnaom.ticketingmanagerserver.mapper.SlaContractMapper;
import com.mapnaom.ticketingmanagerserver.model.Customer;
import com.mapnaom.ticketingmanagerserver.model.SlaContract;
import com.mapnaom.ticketingmanagerserver.repository.CustomerRepository;
import com.mapnaom.ticketingmanagerserver.repository.SlaContractRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SlaContractService {

    private final SlaContractRepository slaContractRepository;
    private final CustomerRepository customerRepository;
    private final SlaContractMapper slaContractMapper;

    // --- Create SLA Contract ---
    @Transactional
    public SlaContractResponseDto createSlaContract(SlaContractRequestDto dto) {
        SlaContract contract = slaContractMapper.toEntity(dto);

        // Resolve and set Customer
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new TicketService.ResourceNotFoundException("Customer not found with id: " + dto.getCustomerId()));
        contract.setCustomer(customer);

        SlaContract savedContract = slaContractRepository.save(contract);
        return slaContractMapper.toResponseDto(savedContract);
    }

    // --- Get All SLA Contracts ---
    public List<SlaContractResponseDto> getAllSlaContracts() {
        return slaContractRepository.findAll().stream()
                .map(slaContractMapper::toResponseDto)
                .toList();
    }

    // --- Get SLA Contract By ID ---
    public SlaContractResponseDto getSlaContractById(Long id) {
        SlaContract contract = slaContractRepository.findById(id)
                .orElseThrow(() -> new TicketService.ResourceNotFoundException("SLA Contract not found with id: " + id));
        return slaContractMapper.toResponseDto(contract);
    }

    // --- Update SLA Contract ---
    @Transactional
    public SlaContractResponseDto updateSlaContract(Long id, SlaContractRequestDto dto) {
        SlaContract existingContract = slaContractRepository.findById(id)
                .orElseThrow(() -> new TicketService.ResourceNotFoundException("SLA Contract not found with id: " + id));

        // Update simple fields using Mapper
        slaContractMapper.updateContractFromDto(dto, existingContract);

        // Handle Customer Relationship manually (Mapper ignores it)
        // We check if the customerId is provided and if it's actually different from the current one
        if (dto.getCustomerId() != null) {
            // If the contract has no customer yet, or if the ID is different
            if (existingContract.getCustomer() == null || !existingContract.getCustomer().getId().equals(dto.getCustomerId())) {
                Customer customer = customerRepository.findById(dto.getCustomerId())
                        .orElseThrow(() -> new TicketService.ResourceNotFoundException("Customer not found with id: " + dto.getCustomerId()));
                existingContract.setCustomer(customer);
            }
        }

        SlaContract updatedContract = slaContractRepository.save(existingContract);
        return slaContractMapper.toResponseDto(updatedContract);
    }

    // --- Delete SLA Contract ---
    @Transactional
    public void deleteSlaContract(Long id) {
        SlaContract contract = slaContractRepository.findById(id)
                .orElseThrow(() -> new TicketService.ResourceNotFoundException("SLA Contract not found with id: " + id));
        slaContractRepository.delete(contract);
    }
}