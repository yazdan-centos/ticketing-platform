package com.mapnaom.ticketingplatform.bootstrap;

import com.mapnaom.ticketingplatform.model.Role;
import com.mapnaom.ticketingplatform.repository.AppUserRepository;
import com.mapnaom.ticketingplatform.repository.CustomerRepository;
import com.mapnaom.ticketingplatform.repository.MeetingRepository;
import com.mapnaom.ticketingplatform.repository.PermissionRepository;
import com.mapnaom.ticketingplatform.repository.RoleRepository;
import com.mapnaom.ticketingplatform.repository.SlaContractRepository;
import com.mapnaom.ticketingplatform.repository.TaskRepository;
import com.mapnaom.ticketingplatform.repository.TeamManagerRepository;
import com.mapnaom.ticketingplatform.repository.TeamMemberRepository;
import com.mapnaom.ticketingplatform.repository.TeamMembershipRepository;
import com.mapnaom.ticketingplatform.repository.TeamRepository;
import com.mapnaom.ticketingplatform.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private TeamManagerRepository teamManagerRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;
    @Mock
    private SlaContractRepository slaContractRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private TeamMembershipRepository teamMembershipRepository;
    @Mock
    private MeetingRepository meetingRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void seedsManagerWhenAnotherTargetTableAlreadyContainsData() throws Exception {
        Role customerRole = role("CUSTOMER");
        Role teamMemberRole = role("TEAM_MEMBER");
        Role teamManagerRole = role("TEAM_MANAGER");

        when(permissionRepository.findAll()).thenReturn(List.of());
        when(roleRepository.count()).thenReturn(1L);
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(roleRepository.findByName("TEAM_MEMBER")).thenReturn(Optional.of(teamMemberRole));
        when(roleRepository.findByName("TEAM_MANAGER")).thenReturn(Optional.of(teamManagerRole));
        when(appUserRepository.existsByUsernameIgnoreCase(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded-password");
        when(customerRepository.count()).thenReturn(1L);
        when(teamManagerRepository.findAll()).thenReturn(List.of());
        when(teamMemberRepository.findAll()).thenReturn(List.of());
        when(teamRepository.findAll()).thenReturn(List.of());
        when(meetingRepository.findAll()).thenReturn(List.of());
        when(taskRepository.findAll()).thenReturn(List.of());

        dataInitializer.run();

        ArgumentCaptor<com.mapnaom.ticketingplatform.model.TeamManager> managerCaptor =
                ArgumentCaptor.forClass(com.mapnaom.ticketingplatform.model.TeamManager.class);
        verify(teamManagerRepository).save(managerCaptor.capture());
        assertThat(managerCaptor.getValue().getUsername()).isEqualTo("manager");
    }

    private Role role(String name) {
        Role role = new Role();
        role.setName(name);
        return role;
    }
}
