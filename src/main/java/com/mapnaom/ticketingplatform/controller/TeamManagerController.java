package com.mapnaom.ticketingmanagerserver.controller;

import com.mapnaom.ticketingmanagerserver.dto.TeamManagerRequestDto;
import com.mapnaom.ticketingmanagerserver.dto.TeamManagerResponseDto;
import com.mapnaom.ticketingmanagerserver.service.TeamManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team-managers")
@RequiredArgsConstructor
public class TeamManagerController {

    private final TeamManagerService teamManagerService;

    // --- Create Team Manager ---
    @PostMapping
    public ResponseEntity<TeamManagerResponseDto> createTeamManager(@Valid @RequestBody TeamManagerRequestDto dto) {
        TeamManagerResponseDto createdManager = teamManagerService.createTeamManager(dto);
        return new ResponseEntity<>(createdManager, HttpStatus.CREATED);
    }

    // --- Get All Team Managers ---
    @GetMapping
    public ResponseEntity<List<TeamManagerResponseDto>> getAllTeamManagers() {
        List<TeamManagerResponseDto> managers = teamManagerService.getAllTeamManagers();
        return ResponseEntity.ok(managers);
    }

    // --- Get Team Manager By ID ---
    @GetMapping("/{id}")
    public ResponseEntity<TeamManagerResponseDto> getTeamManagerById(@PathVariable Long id) {
        TeamManagerResponseDto manager = teamManagerService.getTeamManagerById(id);
        return ResponseEntity.ok(manager);
    }

    // --- Update Team Manager ---
    @PutMapping("/{id}")
    public ResponseEntity<TeamManagerResponseDto> updateTeamManager(
            @PathVariable Long id,
            @Valid @RequestBody TeamManagerRequestDto dto) {
        TeamManagerResponseDto updatedManager = teamManagerService.updateTeamManager(id, dto);
        return ResponseEntity.ok(updatedManager);
    }

    // --- Delete Team Manager ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeamManager(@PathVariable Long id) {
        teamManagerService.deleteTeamManager(id);
        return ResponseEntity.noContent().build();
    }
}