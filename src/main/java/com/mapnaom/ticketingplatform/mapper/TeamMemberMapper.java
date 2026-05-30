package com.mapnaom.ticketingplatform.mapper;

import com.mapnaom.ticketingplatform.dto.TeamMemberRequestDto;
import com.mapnaom.ticketingplatform.dto.TeamMemberResponseDto;
import com.mapnaom.ticketingplatform.model.TeamMember;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface TeamMemberMapper {

    // --- Request to Entity ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "manager", ignore = true) // Service sets this
    @Mapping(target = "assignedTickets", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    TeamMember toEntity(TeamMemberRequestDto dto);

    // --- Entity to Response ---
    @Mapping(target = "managerId", source = "manager", qualifiedByName = "mapManagerId")
    TeamMemberResponseDto toResponseDto(TeamMember entity);

    // --- Update ---
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "assignedTickets", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateMemberFromDto(TeamMemberRequestDto dto, @MappingTarget TeamMember entity);

    @Named("mapManagerId")
    default Long mapManagerId(com.mapnaom.ticketingplatform.model.TeamManager manager) {
        return manager != null ? manager.getId() : null;
    }
}