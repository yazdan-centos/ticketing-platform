package com.mapnaom.ticketingplatform.mapper;

import com.mapnaom.ticketingplatform.dto.TeamManagerRequestDto;
import com.mapnaom.ticketingplatform.dto.TeamManagerResponseDto;
import com.mapnaom.ticketingplatform.model.TeamManager;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TeamManagerMapper {

    // --- Request to Entity ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true) // Roles are assigned via the admin access endpoints, never from a client payload
    @Mapping(target = "teamMembers", ignore = true) // Service handles relationships
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    TeamManager toEntity(TeamManagerRequestDto dto);

    // --- Entity to Response ---
    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRoleNames")
    @Mapping(target = "teamMemberIds", source = "teamMembers", qualifiedByName = "mapMemberIds")
    TeamManagerResponseDto toResponseDto(TeamManager entity);

    // --- Update ---
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "teamMembers", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "password", ignore = true) // Password updates usually handled separately
    void updateManagerFromDto(TeamManagerRequestDto dto, @MappingTarget TeamManager entity);

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

    // --- Helper Methods ---

    // Extracts IDs from the TeamMember entity set to the DTO list of IDs
    @Named("mapMemberIds")
    default Set<Long> mapMemberIds(Set<com.mapnaom.ticketingplatform.model.TeamMember> members) {
        if (members == null) {
            return null;
        }
        return members.stream()
                .map(com.mapnaom.ticketingplatform.model.TeamMember::getId)
                .collect(Collectors.toSet());
    }
}