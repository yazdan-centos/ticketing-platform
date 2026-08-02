package com.mapnaom.ticketingplatform.controller;

import com.mapnaom.ticketingplatform.dto.access.AdminUserDto;
import com.mapnaom.ticketingplatform.dto.access.AdminUserRequestDto;
import com.mapnaom.ticketingplatform.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('USER_READ', 'ACCESS_ADMIN')")
    public List<AdminUserDto> listUsers() {
        return adminUserService.listUsers();
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAnyAuthority('USER_READ', 'USER_UPDATE', 'ACCESS_ADMIN')")
    public List<String> listRoles() {
        return adminUserService.listRoles();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('USER_CREATE', 'ACCESS_ADMIN')")
    public AdminUserDto create(@RequestBody @Valid AdminUserRequestDto request) {
        return adminUserService.create(request);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('USER_UPDATE', 'ACCESS_ADMIN')")
    public AdminUserDto update(@PathVariable Long userId,
                               @RequestBody @Valid AdminUserRequestDto request) {
        return adminUserService.update(userId, request);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('USER_DELETE', 'ACCESS_ADMIN')")
    public void delete(@PathVariable Long userId) {
        adminUserService.delete(userId);
    }
}
