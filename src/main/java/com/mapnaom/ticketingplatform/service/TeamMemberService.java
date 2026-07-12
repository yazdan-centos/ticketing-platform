package com.mapnaom.ticketingplatform.service;

import com.mapnaom.ticketingplatform.dto.TeamMemberRequestDto;
import com.mapnaom.ticketingplatform.dto.TeamMemberResponseDto;
import com.mapnaom.ticketingplatform.dto.TeamMemberSearchCriteriaDto;
import com.mapnaom.ticketingplatform.mapper.TeamMemberMapper;
import com.mapnaom.ticketingplatform.model.TeamMember;
import com.mapnaom.ticketingplatform.model.TeamManager;
import com.mapnaom.ticketingplatform.repository.TeamMemberRepository;
import com.mapnaom.ticketingplatform.repository.TeamManagerRepository;
import com.mapnaom.ticketingplatform.specification.TeamMemberSpecification;
import io.jsonwebtoken.io.IOException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;
    private final TeamManagerRepository teamManagerRepository;
    private final TeamMemberMapper teamMemberMapper;
    private final AvatarStorageService avatarStorageService;

    // --- Create Team Member ---
    @Transactional
    public TeamMemberResponseDto createTeamMember(TeamMemberRequestDto dto) {
        try {
            if (dto == null) {
                throw new IllegalArgumentException("Team member request data cannot be null.");
            }

            // Check uniqueness
            if (dto.getUsername() != null && teamMemberRepository.existsByUsername(dto.getUsername())) {
                throw new IllegalStateException("The username is already taken. Please choose a different one. [Username: " + dto.getUsername() + "]");
            }
            if (dto.getEmail() != null && teamMemberRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalStateException("The email address is already registered. Please use a different one. [Email: " + dto.getEmail() + "]");
            }

            TeamMember member = teamMemberMapper.toEntity(dto);

            // Encode password

            // Resolve Manager
            if (dto.getManagerId() != null) {
                TeamManager manager = teamManagerRepository.findById(dto.getManagerId())
                        .orElseThrow(() -> new EntityNotFoundException("The specified manager could not be found. Please verify the manager ID. [Manager ID: " + dto.getManagerId() + "]"));
                member.setManager(manager);
            }

            TeamMember savedMember = teamMemberRepository.save(member);
            return teamMemberMapper.toResponseDto(savedMember);
        } catch (IllegalArgumentException | IllegalStateException | EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred while creating the team member. Please try again later. [Context: createTeamMember, Username: " + (dto != null ? dto.getUsername() : "null") + "]", e);
        }
    }
/*******************    💫 Codegeex Suggestion    *******************/
    @Transactional
    public TeamMemberResponseDto updateTeamMemberAvatar(Long id, MultipartFile file) {
        if (id == null) {
            throw new IllegalArgumentException("Invalid request: Member ID cannot be null.");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Invalid request: Avatar file cannot be empty.");
        }

        TeamMember member = teamMemberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team Member not found with id: " + id));
        String avatarUrl;
        try {
            avatarUrl = avatarStorageService.storeAvatar("team-members", member.getId(), file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store avatar for Team Member with id: " + id + ". Please try again later.", e);
        } catch (IllegalStateException e) {
            throw new RuntimeException("Invalid file format or size for Team Member with id: " + id + ". Please ensure the file is a valid image within the size limit.", e);
        }

        member.setAvatarUrl(avatarUrl);

        TeamMember savedMember = teamMemberRepository.save(member);
        return teamMemberMapper.toResponseDto(savedMember);
    }

    @Transactional
    public void deleteTeamMemberAvatar(Long id) {
        TeamMember member = teamMemberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team Member not found with id: " + id));

        avatarStorageService.deleteAvatar("team-members", member.getId(), member.getAvatarUrl());
        member.setAvatarUrl(null);
        teamMemberRepository.save(member);
    }
    // --- Get All Team Members ---
    public List<TeamMemberResponseDto> getAllTeamMembers() {
        return teamMemberRepository.findAll().stream()
                .map(teamMemberMapper::toResponseDto)
                .toList();
    }

    // --- Get Team Member By ID ---
    public TeamMemberResponseDto getTeamMemberById(Long id) {
        TeamMember member = teamMemberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team Member not found with id: " + id));
        return teamMemberMapper.toResponseDto(member);
    }

    // --- Search Team Members ---
    public List<TeamMemberResponseDto> searchTeamMembers(TeamMemberSearchCriteriaDto criteria) {
        List<TeamMember> teamMembers = teamMemberRepository.findAll(TeamMemberSpecification.filterTeamMembers(criteria));
        return teamMembers.stream()
                .map(teamMemberMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    // --- Update Team Member ---
    @Transactional
    public TeamMemberResponseDto updateTeamMember(Long id, TeamMemberRequestDto dto) {
        TeamMember existingMember = teamMemberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team Member not found with id: " + id));

        // Map simple fields (ignoring password and manager)
        teamMemberMapper.updateMemberFromDto(dto, existingMember);

        // Handle Manager Assignment
        if (dto.getManagerId() != null) {
            // Only update if the manager ID is actually different
            if (existingMember.getManager() == null || !existingMember.getManager().getId().equals(dto.getManagerId())) {
                TeamManager manager = teamManagerRepository.findById(dto.getManagerId())
                        .orElseThrow(() -> new EntityNotFoundException("Team Manager not found with id: " + dto.getManagerId()));
                existingMember.setManager(manager);
            }
        } else {
            // If managerId is null in DTO, unassign the manager
            // Depending on business logic. Here we clear the existing manager:
            existingMember.setManager(null);
        }

        TeamMember updatedMember = teamMemberRepository.save(existingMember);
        return teamMemberMapper.toResponseDto(updatedMember);
    }

    // --- Delete Team Member (Soft Delete) ---
    @Transactional
    public void deleteTeamMember(Long id) {
        TeamMember member = teamMemberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team Member not found with id: " + id));
        teamMemberRepository.delete(member);
    }
}
