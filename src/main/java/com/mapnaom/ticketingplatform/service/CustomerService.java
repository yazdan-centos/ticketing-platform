package com.mapnaom.ticketingplatform.service;

import com.mapnaom.ticketingplatform.dto.CustomerRequestDto;
import com.mapnaom.ticketingplatform.dto.CustomerResponseDto;
import com.mapnaom.ticketingplatform.mapper.CustomerMapper;
import com.mapnaom.ticketingplatform.model.Customer;
import com.mapnaom.ticketingplatform.repository.CustomerRepository;
import com.mapnaom.ticketingplatform.specification.CustomerSpecification;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    // --- Create Customer ---
    @Transactional
    public CustomerResponseDto createCustomer(CustomerRequestDto dto) {
        // Check if username or email already exists (optional validation)
        if (customerRepository.existsByUsername(dto.getUsername())) {
            throw new EntityNotFoundException("Username already exists: " + dto.getUsername());
        }
        if (customerRepository.existsByEmail(dto.getEmail())) {
            throw new EntityNotFoundException("Email already exists: " + dto.getEmail());
        }

        Customer customer = customerMapper.toEntity(dto);

        // Encode password before saving

        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.toResponseDto(savedCustomer);
    }

    // --- Get All Customers (with pagination and filtering) ---
    public Page<CustomerResponseDto> getAllCustomers(
            String firstName,
            String lastName,
            String username,
            String email,
            String companyName,
            String phone,
            Boolean deleted,
            Pageable pageable
    ) {
        Specification<Customer> spec = CustomerSpecification.filterBy(
                firstName, lastName, username, email, companyName, phone, deleted
        );
        
        return customerRepository.findAll(spec, pageable)
                .map(customerMapper::toResponseDto);
    }

    // --- Get All Customers (non-paginated, for backward compatibility) ---
    public List<CustomerResponseDto> getAllCustomers() {
        // AppUser has @Where(clause = "deleted = false"), so findAll automatically filters deleted users
        return customerRepository.findAll().stream()
                .map(customerMapper::toResponseDto)
                .toList();
    }

    // --- Get Customer By ID ---
    public CustomerResponseDto getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));
        return customerMapper.toResponseDto(customer);
    }

    // --- Update Customer ---
    @Transactional
    public CustomerResponseDto updateCustomer(Long id, CustomerRequestDto dto) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));

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
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));

        // The @SQLDelete annotation on AppUser handles the soft delete logic (sets deleted = true)
        customerRepository.delete(customer);
    }

    // --- Helper for existence checks (if repository doesn't have them) ---
    // You might need to add existsByUsername and existsByEmail to CustomerRepository interface
}
