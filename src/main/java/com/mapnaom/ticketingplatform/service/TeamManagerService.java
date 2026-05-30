package com.mapnaom.ticketingplatform.service;

import com.mapnaom.ticketingplatform.dto.TeamManagerRequestDto;
import com.mapnaom.ticketingplatform.dto.TeamManagerResponseDto;
import com.mapnaom.ticketingplatform.mapper.TeamManagerMapper;
import com.mapnaom.ticketingplatform.model.TeamManager;
import com.mapnaom.ticketingplatform.repository.TeamManagerRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamManagerService {

    private final TeamManagerRepository teamManagerRepository;
    private final TeamManagerMapper teamManagerMapper;

    // --- Create Team Manager ---
    @Transactional
    public TeamManagerResponseDto createTeamManager(TeamManagerRequestDto dto) {
        // Check uniqueness
        if (teamManagerRepository.existsByUsername(dto.getUsername())) {
            throw new EntityNotFoundException("Username already exists: " + dto.getUsername());
        }
        if (teamManagerRepository.existsByEmail(dto.getEmail())) {
            throw new EntityNotFoundException("Email already exists: " + dto.getEmail());
        }

        TeamManager manager = teamManagerMapper.toEntity(dto);



        TeamManager savedManager = teamManagerRepository.save(manager);
        return teamManagerMapper.toResponseDto(savedManager);
    }

    // --- Get All Team Managers ---
    public List<TeamManagerResponseDto> getAllTeamManagers() {
        return teamManagerRepository.findAll().stream()
                .map(teamManagerMapper::toResponseDto)
                .toList();
    }

    // --- Get Team Manager By ID ---
    public TeamManagerResponseDto getTeamManagerById(Long id) {
        TeamManager manager = teamManagerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team Manager not found with id: " + id));
        return teamManagerMapper.toResponseDto(manager);
    }

    // --- Update Team Manager ---
    @Transactional
    public TeamManagerResponseDto updateTeamManager(Long id, TeamManagerRequestDto dto) {
        TeamManager existingManager = teamManagerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team Manager not found with id: " + id));

        // Map simple fields (ignoring password and team members list)
        teamManagerMapper.updateManagerFromDto(dto, existingManager);

        // Note: Updating the list of team members (teamMembers) is usually done
        // by updating the TeamMember entity itself (setting the manager),
        // rather than updating the Manager's list directly.

        TeamManager updatedManager = teamManagerRepository.save(existingManager);
        return teamManagerMapper.toResponseDto(updatedManager);
    }

    // --- Delete Team Manager (Soft Delete) ---
    @Transactional
    public void deleteTeamManager(Long id) {
        TeamManager manager = teamManagerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team Manager not found with id: " + id));

        // Note: If you have database constraints (ON DELETE SET NULL), the teamMembers
        // managed by this manager will have their manager_id set to null.
        // If you want to prevent deletion if they have members, add a check here.

        teamManagerRepository.delete(manager);
    }
}