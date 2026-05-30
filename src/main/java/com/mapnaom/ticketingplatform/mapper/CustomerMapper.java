package com.mapnaom.ticketingmanagerserver.mapper;

import com.mapnaom.ticketingmanagerserver.dto.CustomerRequestDto;
import com.mapnaom.ticketingmanagerserver.dto.CustomerResponseDto;
import com.mapnaom.ticketingmanagerserver.model.Customer;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",implementationName = "Customer.class")
public interface CustomerMapper {

    // --- Request to Entity ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slaContracts", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Customer toEntity(CustomerRequestDto dto);

    // --- Entity to Response ---
    @Mapping(target = "slaContractIds", source = "slaContracts", qualifiedByName = "mapContractIds")
    CustomerResponseDto toResponseDto(Customer entity);

    // --- Update ---
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slaContracts", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "password", ignore = true) // Usually handled separately via a change password endpoint
    void updateCustomerFromDto(CustomerRequestDto dto, @MappingTarget Customer entity);

    // Helper method to extract IDs from the entity set to the DTO list
    @Named("mapContractIds")
    default Set<Long> mapContractIds(Set<com.mapnaom.ticketingmanagerserver.model.SlaContract> contracts) {
        if (contracts == null) {
            return null;
        }
        return contracts.stream()
                .map(com.mapnaom.ticketingmanagerserver.model.SlaContract::getId)
                .collect(Collectors.toSet());
    }
}