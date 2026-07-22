package com.mapnaom.ticketingplatform.service.impl;

import com.mapnaom.ticketingplatform.dto.team.TeamCreateRequest;
import com.mapnaom.ticketingplatform.dto.team.TeamResponse;
import com.mapnaom.ticketingplatform.mapper.TeamMapper;
import com.mapnaom.ticketingplatform.model.AppUser;
import com.mapnaom.ticketingplatform.model.Team;
import com.mapnaom.ticketingplatform.model.TeamMembership;
import com.mapnaom.ticketingplatform.model.enums.TeamRole;
import com.mapnaom.ticketingplatform.repository.AppUserRepository;
import com.mapnaom.ticketingplatform.repository.TeamMembershipRepository;
import com.mapnaom.ticketingplatform.repository.TeamRepository;
import com.mapnaom.ticketingplatform.service.TeamService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    private final TeamMembershipRepository membershipRepository;
    private final AppUserRepository userRepository;
    private final TeamMapper teamMapper;

    @Override
    public TeamResponse createTeam(TeamCreateRequest request) {
        String name = request.name().trim();
        if (teamRepository.existsByNameIgnoreCaseAndActiveTrue(name)) {
            throw new IllegalArgumentException("An active team already exists with name: " + name);
        }
        Team team = new Team();
        team.setName(name);
        team.setDescription(request.description());
        return teamMapper.toResponse(teamRepository.save(team));
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse getTeam(Long teamId) {
        return teamMapper.toResponse(findActiveTeam(teamId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeamResponse> getTeams(Pageable pageable) {
        return teamRepository.findByActiveTrue(pageable).map(teamMapper::toResponse);
    }

    @Override
    public void addMember(Long teamId, Long userId, TeamRole role) {
        Team team = findActiveTeam(teamId);
        AppUser user = findActiveUser(userId);
        TeamMembership membership = membershipRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseGet(TeamMembership::new);
        membership.setTeam(team);
        membership.setUser(user);
        membership.setRole(role);
        membershipRepository.save(membership);
    }

    @Override
    public void removeMember(Long teamId, Long userId) {
        findActiveTeam(teamId);
        TeamMembership membership = membershipRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Team membership not found for team " + teamId + " and user " + userId));
        membershipRepository.delete(membership);
    }

    @Override
    public void deleteTeam(Long teamId) {
        Team team = findActiveTeam(teamId);
        team.setActive(false);
        teamRepository.save(team);
    }

    private Team findActiveTeam(Long teamId) {
        return teamRepository.findByIdAndActiveTrue(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Active team not found with id: " + teamId));
    }

    private AppUser findActiveUser(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new EntityNotFoundException("Active user not found with id: " + userId);
        }
        return user;
    }
}
