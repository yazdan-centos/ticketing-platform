package com.mapnaom.ticketingmanagerserver.mapper;

import com.mapnaom.ticketingmanagerserver.dto.SlaContractRequestDto;
import com.mapnaom.ticketingmanagerserver.dto.SlaContractResponseDto;
import com.mapnaom.ticketingmanagerserver.model.SlaContract;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = {CustomerMapper.class})
public interface SlaContractMapper {

    // --- Request to Entity ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true) // Service sets this
    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SlaContract toEntity(SlaContractRequestDto dto);

    // --- Entity to Response ---
    // Uses CustomerMapper (via 'uses') to convert the Customer entity to CustomerResponseDto
    @Mapping(target = "customer", source = "customer")
    SlaContractResponseDto toResponseDto(SlaContract entity);

    // --- Update ---
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateContractFromDto(SlaContractRequestDto dto, @MappingTarget SlaContract entity);
}