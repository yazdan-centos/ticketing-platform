package com.mapnaom.ticketingplatform.mapper;

import com.mapnaom.ticketingplatform.dto.CustomerRequestDto;
import com.mapnaom.ticketingplatform.dto.CustomerResponseDto;
import com.mapnaom.ticketingplatform.model.Customer;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    // --- Request to Entity ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true) // Roles are assigned via the admin access endpoints, never from a client payload
    @Mapping(target = "slaContracts", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Customer toEntity(CustomerRequestDto dto);

    // --- Entity to Response ---
    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRoleNames")
    @Mapping(target = "slaContractIds", source = "slaContracts", qualifiedByName = "mapContractIds")
    CustomerResponseDto toResponseDto(Customer entity);

    // --- Update ---
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "slaContracts", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "password", ignore = true) // Usually handled separately via a change password endpoint
    void updateCustomerFromDto(CustomerRequestDto dto, @MappingTarget Customer entity);

    // Maps the entity's role entities to their names for the response DTO.
    @Named("mapRoleNames")
    default Set<String> mapRoleNames(Set<com.mapnaom.ticketingplatform.model.Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(com.mapnaom.ticketingplatform.model.Role::getName)
                .collect(Collectors.toSet());
    }

    // Helper method to extract IDs from the entity set to the DTO list
    @Named("mapContractIds")
    default Set<Long> mapContractIds(Set<com.mapnaom.ticketingplatform.model.SlaContract> contracts) {
        if (contracts == null) {
            return null;
        }
        return contracts.stream()
                .map(com.mapnaom.ticketingplatform.model.SlaContract::getId)
                .collect(Collectors.toSet());
    }
}