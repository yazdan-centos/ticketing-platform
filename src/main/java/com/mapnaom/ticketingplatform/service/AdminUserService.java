package com.mapnaom.ticketingplatform.service;

import com.mapnaom.ticketingplatform.dto.access.AdminUserDto;
import com.mapnaom.ticketingplatform.dto.access.AdminUserRequestDto;
import com.mapnaom.ticketingplatform.model.AppUser;
import com.mapnaom.ticketingplatform.model.Customer;
import com.mapnaom.ticketingplatform.model.Role;
import com.mapnaom.ticketingplatform.model.TeamManager;
import com.mapnaom.ticketingplatform.model.TeamMember;
import com.mapnaom.ticketingplatform.repository.AppUserRepository;
import com.mapnaom.ticketingplatform.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<AdminUserDto> listUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !Boolean.TRUE.equals(user.getDeleted()))
                .sorted(Comparator.comparing(AppUser::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> listRoles() {
        return roleRepository.findAll().stream().map(Role::getName).sorted().toList();
    }

    @Transactional
    public AdminUserDto create(AdminUserRequestDto request) {
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("Password is required when creating a user");
        }
        String username = request.username().trim();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalStateException("Username is already in use");
        }
        Set<Role> roles = resolveRoles(request.roles());
        AppUser user = newUser(roles);
        apply(user, request, roles);
        user.setPassword(passwordEncoder.encode(request.password()));
        return toDto(userRepository.save(user));
    }

    @Transactional
    public AdminUserDto update(Long userId, AdminUserRequestDto request) {
        AppUser user = getUser(userId);
        String username = request.username().trim();
        if (userRepository.existsByUsernameIgnoreCaseAndIdNot(username, userId)) {
            throw new IllegalStateException("Username is already in use");
        }
        apply(user, request, resolveRoles(request.roles()));
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        return toDto(userRepository.save(user));
    }

    @Transactional
    public void delete(Long userId) {
        AppUser user = getUser(userId);
        if (userRepository.hasAssociatedData(userId)) {
            throw new IllegalStateException("User cannot be deleted because associated data exists");
        }
        userRepository.delete(user);
    }

    private void apply(AppUser user, AdminUserRequestDto request, Set<Role> roles) {
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim());
        user.setRoles(new HashSet<>(roles));
    }

    private AppUser newUser(Set<Role> roles) {
        Set<String> names = roles.stream().map(Role::getName).collect(Collectors.toSet());
        if (names.contains("TEAM_MANAGER")) return new TeamManager();
        if (names.contains("TEAM_MEMBER")) return new TeamMember();
        return new Customer();
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        return roleNames.stream()
                .map(name -> name.trim().toUpperCase(Locale.ROOT))
                .map(name -> roleRepository.findByName(name)
                        .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + name)))
                .collect(Collectors.toSet());
    }

    private AppUser getUser(Long userId) {
        return userRepository.findById(userId)
                .filter(user -> !Boolean.TRUE.equals(user.getDeleted()))
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }

    private AdminUserDto toDto(AppUser user) {
        Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        String userType = user instanceof TeamManager ? "TEAM_MANAGER"
                : user instanceof TeamMember ? "TEAM_MEMBER" : "CUSTOMER";
        String fullName = ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                + (user.getLastName() == null ? "" : user.getLastName())).trim();
        return new AdminUserDto(user.getId(), user.getFirstName(), user.getLastName(), fullName,
                user.getUsername(), user.getEmail(), user.getAvatarUrl(), userType, roles,
                !userRepository.hasAssociatedData(user.getId()), user.getCreatedAt(), user.getUpdatedAt());
    }
}
