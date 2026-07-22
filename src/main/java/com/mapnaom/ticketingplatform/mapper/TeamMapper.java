package com.mapnaom.ticketingplatform.mapper;

import com.mapnaom.ticketingplatform.dto.team.TeamMemberResponse;
import com.mapnaom.ticketingplatform.dto.team.TeamResponse;
import com.mapnaom.ticketingplatform.model.Team;
import com.mapnaom.ticketingplatform.model.TeamMembership;
import org.springframework.stereotype.Component;

@Component
public class TeamMapper {
    public TeamResponse toResponse(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.isActive(),
                team.getCreatedAt(),
                team.getMemberships().stream().map(this::toMemberResponse).toList()
        );
    }

    private TeamMemberResponse toMemberResponse(TeamMembership membership) {
        return new TeamMemberResponse(
                membership.getUser().getId(),
                membership.getUser().getUsername(),
                membership.getUser().getFullName().trim(),
                membership.getRole(),
                membership.getJoinedAt()
        );
    }
}
