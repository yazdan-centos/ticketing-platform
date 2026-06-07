package com.mapnaom.ticketingplatform.controller;

import com.mapnaom.ticketingplatform.dto.CustomerRequestDto;
import com.mapnaom.ticketingplatform.dto.CustomerResponseDto;
import com.mapnaom.ticketingplatform.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // --- Create Customer ---
    @PostMapping
    public ResponseEntity<CustomerResponseDto> createCustomer(@Valid @RequestBody CustomerRequestDto dto) {
        CustomerResponseDto createdCustomer = customerService.createCustomer(dto);
        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }

    // --- Get All Customers (Paginated with Filtering) ---
    @GetMapping("/search")
    public ResponseEntity<Page<CustomerResponseDto>> searchCustomers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Boolean deleted,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<CustomerResponseDto> customers = customerService.getAllCustomers(
                firstName, lastName, username, email, companyName, phone, deleted, pageable
        );
        return ResponseEntity.ok(customers);
    }

    // --- Get All Customers (Non-paginated, for backward compatibility) ---
    @GetMapping
    public ResponseEntity<List<CustomerResponseDto>> getAllCustomers() {
        List<CustomerResponseDto> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    // --- Get Customer By ID ---
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDto> getCustomerById(@PathVariable Long id) {
        CustomerResponseDto customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(customer);
    }

    // --- Update Customer ---
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDto> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequestDto dto) {
        CustomerResponseDto updatedCustomer = customerService.updateCustomer(id, dto);
        return ResponseEntity.ok(updatedCustomer);
    }

    // --- Delete Customer ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
