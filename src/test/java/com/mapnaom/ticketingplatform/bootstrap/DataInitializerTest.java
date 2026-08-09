package com.mapnaom.ticketingplatform.bootstrap;

import com.mapnaom.ticketingplatform.model.Permission;
import com.mapnaom.ticketingplatform.model.Role;
import com.mapnaom.ticketingplatform.model.enums.AccessScope;
import com.mapnaom.ticketingplatform.repository.AppUserRepository;
import com.mapnaom.ticketingplatform.repository.PermissionRepository;
import com.mapnaom.ticketingplatform.repository.RoleRepository;
import com.mapnaom.ticketingplatform.repository.TaskRepository;
import com.mapnaom.ticketingplatform.repository.TicketRepository;
import com.mapnaom.ticketingplatform.repository.UserPermissionGrantRepository;
import com.mapnaom.ticketingplatform.repository.UserResourceScopeRepository;
import com.mapnaom.ticketingplatform.model.UserPermissionGrant;
import com.mapnaom.ticketingplatform.model.UserResourceScope;
import com.mapnaom.ticketingplatform.model.enums.GrantEffect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(DataInitializer.class)
class DataInitializerTest {

    @Autowired
    private DataInitializer dataInitializer;
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserPermissionGrantRepository userPermissionGrantRepository;
    @Autowired
    private UserResourceScopeRepository userResourceScopeRepository;

    @Test
    void repeatedExecutionDeletesDependentDataAndRecreatesDeterministicAccessData() throws Exception {
        dataInitializer.run();

        long userCount = appUserRepository.count();
        long permissionCount = permissionRepository.count();
        long roleCount = roleRepository.count();
        long ticketCount = ticketRepository.count();
        long taskCount = taskRepository.count();

        var manager = appUserRepository.findByUsername("manager").orElseThrow();
        Permission ticketRead = permissionRepository.findByCode("TICKET_READ").orElseThrow();

        UserPermissionGrant grant = new UserPermissionGrant();
        grant.setUser(manager);
        grant.setPermission(ticketRead);
        grant.setEffect(GrantEffect.DENY);
        userPermissionGrantRepository.save(grant);

        UserResourceScope scope = new UserResourceScope();
        scope.setUser(manager);
        scope.setResourceType("TICKET");
        scope.setScope(AccessScope.ALL);
        userResourceScopeRepository.save(scope);

        dataInitializer.run();

        assertThat(appUserRepository.count()).isEqualTo(userCount);
        assertThat(permissionRepository.count()).isEqualTo(permissionCount);
        assertThat(roleRepository.count()).isEqualTo(roleCount);
        assertThat(ticketRepository.count()).isEqualTo(ticketCount);
        assertThat(taskRepository.count()).isEqualTo(taskCount);
        assertThat(userPermissionGrantRepository.count()).isZero();
        assertThat(userResourceScopeRepository.count()).isZero();

        Map<String, Set<String>> permissionCodesByRole = roleRepository.findAll().stream()
                .collect(Collectors.toMap(
                        Role::getName,
                        role -> role.getPermissions().stream()
                                .map(Permission::getCode)
                                .collect(Collectors.toSet())));

        assertThat(permissionCodesByRole.get("CUSTOMER"))
                .containsExactlyInAnyOrder("TICKET_CREATE", "TICKET_READ", "SLA_READ");
        assertThat(permissionCodesByRole.get("TEAM_MEMBER"))
                .containsExactlyInAnyOrder(
                        "TICKET_READ", "TICKET_UPDATE", "CUSTOMER_READ", "SLA_READ",
                        "TEAM_READ", "MEETING_CREATE", "MEETING_READ", "MEETING_UPDATE",
                        "TASK_READ", "TASK_UPDATE");
        assertThat(permissionCodesByRole.get("TEAM_MANAGER"))
                .containsExactlyInAnyOrderElementsOf(
                        permissionRepository.findAll().stream()
                                .map(Permission::getCode)
                                .collect(Collectors.toSet()));
    }
}
