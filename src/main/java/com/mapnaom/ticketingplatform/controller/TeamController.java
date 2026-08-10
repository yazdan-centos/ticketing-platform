package com.mapnaom.ticketingplatform.controller;

import com.mapnaom.ticketingplatform.dto.ApiResponse;
import com.mapnaom.ticketingplatform.dto.team.TeamCreateRequest;
import com.mapnaom.ticketingplatform.dto.team.TeamResponse;
import com.mapnaom.ticketingplatform.model.enums.TeamRole;
import com.mapnaom.ticketingplatform.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @PostMapping
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(
            @Valid @RequestBody TeamCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(teamService.createTeam(request), "Team created"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TeamResponse>>> getTeams(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getTeams(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamResponse>> getTeam(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getTeam(id)));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> addMember(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam TeamRole role) {
        teamService.addMember(id, userId, role);
        return ResponseEntity.ok(ApiResponse.success(null, "Team member added"));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId) {
        teamService.removeMember(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Team member removed"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Team deleted"));
    }
}
