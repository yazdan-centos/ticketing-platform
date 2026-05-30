package com.mapnaom.ticketingmanagerserver.controller;

import com.mapnaom.ticketingmanagerserver.dto.TeamMemberRequestDto;
import com.mapnaom.ticketingmanagerserver.dto.TeamMemberResponseDto;
import com.mapnaom.ticketingmanagerserver.service.TeamMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team-members")
@RequiredArgsConstructor
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    // --- Create Team Member ---
    @PostMapping
    public ResponseEntity<TeamMemberResponseDto> createTeamMember(@Valid @RequestBody TeamMemberRequestDto dto) {
        TeamMemberResponseDto createdMember = teamMemberService.createTeamMember(dto);
        return new ResponseEntity<>(createdMember, HttpStatus.CREATED);
    }

    // --- Get All Team Members ---
    @GetMapping
    public ResponseEntity<List<TeamMemberResponseDto>> getAllTeamMembers() {
        List<TeamMemberResponseDto> members = teamMemberService.getAllTeamMembers();
        return ResponseEntity.ok(members);
    }

    // --- Get Team Member By ID ---
    @GetMapping("/{id}")
    public ResponseEntity<TeamMemberResponseDto> getTeamMemberById(@PathVariable Long id) {
        TeamMemberResponseDto member = teamMemberService.getTeamMemberById(id);
        return ResponseEntity.ok(member);
    }

    // --- Update Team Member ---
    @PutMapping("/{id}")
    public ResponseEntity<TeamMemberResponseDto> updateTeamMember(
            @PathVariable Long id,
            @Valid @RequestBody TeamMemberRequestDto dto) {
        TeamMemberResponseDto updatedMember = teamMemberService.updateTeamMember(id, dto);
        return ResponseEntity.ok(updatedMember);
    }

    // --- Delete Team Member ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeamMember(@PathVariable Long id) {
        teamMemberService.deleteTeamMember(id);
        return ResponseEntity.noContent().build();
    }
}