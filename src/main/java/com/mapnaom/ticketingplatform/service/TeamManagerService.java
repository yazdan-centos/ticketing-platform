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

/**
 * Service class responsible for managing Team Manager entities.
 * Provides core CRUD operations for team managers, including creation with uniqueness checks,
 * retrieval, updating, and soft deletion.
 *
 * <p>Core functionalities include:
 * <ul>
 *   <li>Creating a new team manager while enforcing username and email uniqueness.</li>
 *   <li>Retrieving all team managers or a specific manager by ID.</li>
 *   <li>Updating existing team manager details (excluding password and team members list).</li>
 *   <li>Deleting a team manager (soft delete), which may orphan team members based on DB constraints.</li>
 * </ul>
 *
 * <p>Constructor parameters:
 * <ul>
 *   <li>{@code teamManagerRepository} - Repository for performing database operations on TeamManager entities.</li>
 *   <li>{@code teamManagerMapper} - Mapper for converting between TeamManager entities and DTOs.</li>
 * </ul>
 *
 * <p><b>Usage Example:</b>
 * <pre>
 * &#64;Autowired
 * private TeamManagerService;
 *
 * public void createManager() {
 *     TeamManagerRequestDto dto = new TeamManagerRequestDto("john_doe", "john@example.com");
 *     TeamManagerResponseDto response = teamManagerService.createTeamManager(dto);
 * }
 * </pre>
 *
 * <p><b>Restrictions & Side Effects:</b>
 * <ul>
 *   <li>Creation throws {@code EntityNotFoundException} if the username or email already exists.</li>
 *   <li>Update operations do not modify the manager's password or their associated team members list.
 *       Team members should be updated via the TeamMember entity directly.</li>
 *   <li>Deletion relies on database constraints (e.g., ON DELETE SET NULL). Deleting a manager may
 *       result in orphaned team members (their manager_id will be set to null) unless explicitly
 *       prevented by adding a check before deletion.</li>
 * </ul>
 */
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
