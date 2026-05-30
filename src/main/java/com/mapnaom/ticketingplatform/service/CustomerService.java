package com.mapnaom.ticketingmanagerserver.service;

import com.mapnaom.ticketingmanagerserver.dto.CustomerRequestDto;
import com.mapnaom.ticketingmanagerserver.dto.CustomerResponseDto;
import com.mapnaom.ticketingmanagerserver.mapper.CustomerMapper;
import com.mapnaom.ticketingmanagerserver.model.Customer;
import com.mapnaom.ticketingmanagerserver.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder; // Injected to encode passwords

    // --- Create Customer ---
    @Transactional
    public CustomerResponseDto createCustomer(CustomerRequestDto dto) {
        // Check if username or email already exists (optional validation)
        if (customerRepository.existsByUsername(dto.getUsername())) {
            throw new TicketService.ResourceNotFoundException("Username already exists: " + dto.getUsername());
        }
        if (customerRepository.existsByEmail(dto.getEmail())) {
            throw new TicketService.ResourceNotFoundException("Email already exists: " + dto.getEmail());
        }

        Customer customer = customerMapper.toEntity(dto);

        // Encode password before saving
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));

        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.toResponseDto(savedCustomer);
    }

    // --- Get All Customers ---
    public List<CustomerResponseDto> getAllCustomers() {
        // AppUser has @Where(clause = "deleted = false"), so findAll automatically filters deleted users
        return customerRepository.findAll().stream()
                .map(customerMapper::toResponseDto)
                .toList();
    }

    // --- Get Customer By ID ---
    public CustomerResponseDto getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new TicketService.ResourceNotFoundException("Customer not found with id: " + id));
        return customerMapper.toResponseDto(customer);
    }

    // --- Update Customer ---
    @Transactional
    public CustomerResponseDto updateCustomer(Long id, CustomerRequestDto dto) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new TicketService.ResourceNotFoundException("Customer not found with id: " + id));

        // Map non-null fields from DTO to Entity
        // Note: The mapper is configured to ignore 'password' in update methods.
        // If you want to update passwords, create a separate changePassword method.
        customerMapper.updateCustomerFromDto(dto, existingCustomer);

        // If the DTO logic requires updating password specifically, handle it here:
        // if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
        //     existingCustomer.setPassword(passwordEncoder.encode(dto.getPassword()));
        // }

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        return customerMapper.toResponseDto(updatedCustomer);
    }

    // --- Delete Customer (Soft Delete) ---
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new TicketService.ResourceNotFoundException("Customer not found with id: " + id));

        // The @SQLDelete annotation on AppUser handles the soft delete logic (sets deleted = true)
        customerRepository.delete(customer);
    }

    // --- Helper for existence checks (if repository doesn't have them) ---
    // You might need to add existsByUsername and existsByEmail to CustomerRepository interface
}