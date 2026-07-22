package com.mapnaom.ticketingplatform.service;

import com.mapnaom.ticketingplatform.dto.team.TeamCreateRequest;
import com.mapnaom.ticketingplatform.dto.team.TeamResponse;
import com.mapnaom.ticketingplatform.model.enums.TeamRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TeamService {
    TeamResponse createTeam(TeamCreateRequest request);

    TeamResponse getTeam(Long teamId);

    Page<TeamResponse> getTeams(Pageable pageable);

    void addMember(Long teamId, Long userId, TeamRole role);

    void removeMember(Long teamId, Long userId);

    void deleteTeam(Long teamId);
}
