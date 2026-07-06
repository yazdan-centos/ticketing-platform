package com.mapnaom.ticketingplatform.controller;

import com.mapnaom.ticketingplatform.dto.TeamMemberRequestDto;
import com.mapnaom.ticketingplatform.dto.TeamMemberResponseDto;
import com.mapnaom.ticketingplatform.dto.TeamMemberSearchCriteriaDto;
import com.mapnaom.ticketingplatform.dto.ticket.TicketMessageCreateRequest;
import com.mapnaom.ticketingplatform.dto.ticket.TicketMessageResponse;
import com.mapnaom.ticketingplatform.model.AppUserDetails;
import com.mapnaom.ticketingplatform.service.TicketMessageService;
import com.mapnaom.ticketingplatform.service.TeamMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin

@RestController
@RequestMapping("/api/team-members")
@RequiredArgsConstructor
public class TeamMemberController {

    private final TeamMemberService teamMemberService;
    private final TicketMessageService ticketMessageService;

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

    // --- Search Team Members ---
    @GetMapping("/search")
    public ResponseEntity<List<TeamMemberResponseDto>> searchTeamMembers(TeamMemberSearchCriteriaDto criteria) {
        List<TeamMemberResponseDto> members = teamMemberService.searchTeamMembers(criteria);
        return ResponseEntity.ok(members);
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

    // --- Create Ticket Message as Team Member ---
    @PostMapping("/tickets/{ticketId}/messages")
    public ResponseEntity<TicketMessageResponse> createTicketMessage(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal AppUserDetails principal,
            @Valid @RequestBody TicketMessageCreateRequest request) {
        TicketMessageResponse createdMessage = ticketMessageService.addMessageByTeamMember(
                ticketId,
                request,
                principal.getId());
        return new ResponseEntity<>(createdMessage, HttpStatus.CREATED);
    }
}
