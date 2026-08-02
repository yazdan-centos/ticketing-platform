package com.mapnaom.ticketingplatform.controller;

import com.mapnaom.ticketingplatform.dto.TeamMemberRequestDto;
import com.mapnaom.ticketingplatform.dto.TeamMemberResponseDto;
import com.mapnaom.ticketingplatform.dto.TeamMemberSearchCriteriaDto;
import com.mapnaom.ticketingplatform.dto.ticket.TicketMessageCreateRequest;
import com.mapnaom.ticketingplatform.dto.ticket.TicketMessageResponse;
import com.mapnaom.ticketingplatform.model.AppUserDetails;
import com.mapnaom.ticketingplatform.service.TicketMessageService;
import com.mapnaom.ticketingplatform.service.TeamMemberService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
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

    // --- Upload Team Member Avatar ---
    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TeamMemberResponseDto> uploadTeamMemberAvatar(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("Invalid member ID provided. Please provide a valid positive ID.");
            }
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Avatar file cannot be empty. Please select a valid file to upload.");
            }
            TeamMemberResponseDto updatedMember = teamMemberService.updateTeamMemberAvatar(id, file);
            return ResponseEntity.ok(updatedMember);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(413).build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // --- Delete Team Member Avatar ---
    @DeleteMapping("/{id}/avatar")
    public ResponseEntity<Void> deleteTeamMemberAvatar(@PathVariable Long id) {
        teamMemberService.deleteTeamMemberAvatar(id);
        return ResponseEntity.noContent().build();
    }

    // --- Create Ticket Message as Team Member ---
    @PostMapping("/tickets/{ticketId}/messages")
    @PreAuthorize("hasAuthority('TICKET_UPDATE')")
    public ResponseEntity<?> createTicketMessage(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal AppUserDetails principal,
            @Valid @RequestBody TicketMessageCreateRequest request) {
        try {
            if (ticketId == null || ticketId <= 0) {
                return ResponseEntity.badRequest().body("Invalid ticket ID provided. Please use a valid positive number.");
            }
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication is required to post a message.");
            }
            TicketMessageResponse createdMessage = ticketMessageService.addMessageByTeamMember(
                    ticketId,
                    request,
                    principal.getId());
            return new ResponseEntity<>(createdMessage, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The ticket or user you are trying to access does not exist. Ticket ID: " + ticketId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid message content provided for Ticket ID: " + ticketId + ". Details: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred while adding your message. Please try again later. Ticket ID: " + ticketId);
        }
    }
}
