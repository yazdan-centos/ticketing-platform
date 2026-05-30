package com.mapnaom.ticketingmanagerserver.service;

import com.mapnaom.ticketingmanagerserver.dto.TeamMemberRequestDto;
import com.mapnaom.ticketingmanagerserver.dto.TeamMemberResponseDto;
import com.mapnaom.ticketingmanagerserver.mapper.TeamMemberMapper;
import com.mapnaom.ticketingmanagerserver.model.TeamMember;
import com.mapnaom.ticketingmanagerserver.model.TeamManager;
import com.mapnaom.ticketingmanagerserver.repository.TeamMemberRepository;
import com.mapnaom.ticketingmanagerserver.repository.TeamManagerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;
    private final TeamManagerRepository teamManagerRepository;
    private final TeamMemberMapper teamMemberMapper;
    private final PasswordEncoder passwordEncoder;

    // --- Create Team Member ---
    @Transactional
    public TeamMemberResponseDto createTeamMember(TeamMemberRequestDto dto) {
        // Check uniqueness
        if (teamMemberRepository.existsByUsername(dto.getUsername())) {
            throw new TicketService.ResourceNotFoundException("Username already exists: " + dto.getUsername());
        }
        if (teamMemberRepository.existsByEmail(dto.getEmail())) {
            throw new TicketService.ResourceNotFoundException("Email already exists: " + dto.getEmail());
        }

        TeamMember member = teamMemberMapper.toEntity(dto);

        // Encode password
        member.setPassword(passwordEncoder.encode(member.getPassword()));

        // Resolve Manager
        if (dto.getManagerId() != null) {
            TeamManager manager = teamManagerRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new TicketService.ResourceNotFoundException("Team Manager not found with id: " + dto.getManagerId()));
            member.setManager(manager);
        }

        TeamMember savedMember = teamMemberRepository.save(member);
        return teamMemberMapper.toResponseDto(savedMember);
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
                .orElseThrow(() -> new TicketService.ResourceNotFoundException("Team Member not found with id: " + id));
        return teamMemberMapper.toResponseDto(member);
    }

    // --- Update Team Member ---
    @Transactional
    public TeamMemberResponseDto updateTeamMember(Long id, TeamMemberRequestDto dto) {
        TeamMember existingMember = teamMemberRepository.findById(id)
                .orElseThrow(() -> new TicketService.ResourceNotFoundException("Team Member not found with id: " + id));

        // Map simple fields (ignoring password and manager)
        teamMemberMapper.updateMemberFromDto(dto, existingMember);

        // Handle Manager Assignment
        if (dto.getManagerId() != null) {
            // Only update if the manager ID is actually different
            if (existingMember.getManager() == null || !existingMember.getManager().getId().equals(dto.getManagerId())) {
                TeamManager manager = teamManagerRepository.findById(dto.getManagerId())
                        .orElseThrow(() -> new TicketService.ResourceNotFoundException("Team Manager not found with id: " + dto.getManagerId()));
                existingMember.setManager(manager);
            }
        } else {
            // If managerId is null in DTO, should we unassign?
            // Depending on business logic. Here we keep existing if null, or uncomment below to clear:
            // existingMember.setManager(null);
        }

        TeamMember updatedMember = teamMemberRepository.save(existingMember);
        return teamMemberMapper.toResponseDto(updatedMember);
    }

    // --- Delete Team Member (Soft Delete) ---
    @Transactional
    public void deleteTeamMember(Long id) {
        TeamMember member = teamMemberRepository.findById(id)
                .orElseThrow(() -> new TicketService.ResourceNotFoundException("Team Member not found with id: " + id));
        teamMemberRepository.delete(member);
    }
}