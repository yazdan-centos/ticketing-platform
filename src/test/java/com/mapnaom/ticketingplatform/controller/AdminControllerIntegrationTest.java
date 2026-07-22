package com.mapnaom.ticketingplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapnaom.ticketingplatform.dto.access.GrantDto;
import com.mapnaom.ticketingplatform.dto.access.RolePermissionsUpdateDto;
import com.mapnaom.ticketingplatform.dto.access.ScopeUpdateDto;
import com.mapnaom.ticketingplatform.model.AccessScope;
import com.mapnaom.ticketingplatform.model.AppUser;
import com.mapnaom.ticketingplatform.model.Customer;
import com.mapnaom.ticketingplatform.model.Permission;
import com.mapnaom.ticketingplatform.model.Role;
import com.mapnaom.ticketingplatform.model.TeamMember;
import com.mapnaom.ticketingplatform.model.enums.GrantEffect;
import com.mapnaom.ticketingplatform.repository.*;
import com.mapnaom.ticketingplatform.service.AccessAdminService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AdminController.
 * Tests all API endpoints with real database interactions.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // each test (and its setUp) runs in a rolled-back transaction for isolation
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserPermissionGrantRepository grantRepository;

    @Autowired
    private UserResourceScopeRepository scopeRepository;

    @Autowired
    private AccessAdminService accessAdminService;

    private AppUser testUser;
    private TeamMember testTeamMember;
    private Role testRole;
    private Permission testPermission;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up existing data
        grantRepository.deleteAll();
        scopeRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();

        // Create test permission
        testPermission = new Permission();
        testPermission.setCode("TICKET_READ");
        testPermission.setDescription("Can read tickets");
        testPermission = permissionRepository.save(testPermission);

        Permission permission2 = new Permission();
        permission2.setCode("TICKET_WRITE");
        permission2.setDescription("Can write tickets");
        permissionRepository.save(permission2);

        Permission accessAdminPermission = new Permission();
        accessAdminPermission.setCode("ACCESS_ADMIN");
        accessAdminPermission.setDescription("Can manage access control");
        permissionRepository.save(accessAdminPermission);

        // Create test role
        testRole = new Role();
        testRole.setName("CUSTOMER");
        testRole.setPermissions(new HashSet<>(Arrays.asList(testPermission)));
        testRole = roleRepository.save(testRole);

        Role adminRole = new Role();
        adminRole.setName("TEAM_MANAGER");
        adminRole.setPermissions(new HashSet<>(Arrays.asList(accessAdminPermission)));
        roleRepository.save(adminRole);

        // Create test user (a concrete AppUser; username left unset to avoid
        // colliding with soft-deleted rows from prior tests).
        Customer customer = new Customer();
        customer.setEmail("test@example.com");
        customer.setPassword("password123");
        customer.setRoles(new HashSet<>(Arrays.asList(testRole)));
        testUser = userRepository.save(customer);

        TeamMember teamMember = new TeamMember();
        teamMember.setEmail("member@example.com");
        teamMember.setPassword("password123");
        teamMember.setRoles(new HashSet<>(Arrays.asList(testRole)));
        testTeamMember = userRepository.save(teamMember);
    }

    // ==================== Permission Catalog Tests ====================

    @Test
    @Order(1)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testListPermissions_Success() throws Exception {
        mockMvc.perform(get("/api/admin/access/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[*].code", hasItem("TICKET_READ")))
                .andExpect(jsonPath("$[*].code", hasItem("TICKET_WRITE")))
                .andExpect(jsonPath("$[*].code", hasItem("ACCESS_ADMIN")));
    }

    @Test
    @Order(2)
    @WithMockUser(authorities = {"TICKET_READ"})
    void testListPermissions_Forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/access/permissions"))
                .andExpect(status().isForbidden());
    }

    // ==================== Effective Access Tests ====================

    @Test
    @Order(3)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testGetEffectiveAccess_Success() throws Exception {
        mockMvc.perform(get("/api/admin/access/users/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.roleNames", hasItem("CUSTOMER")))
                .andExpect(jsonPath("$.permissionCodes", hasItem("TICKET_READ")))
                .andExpect(jsonPath("$.scopes.TICKET").exists());
    }

    @Test
    @Order(4)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testGetTeamMemberPermissionStatus_Success() throws Exception {
        mockMvc.perform(get("/api/admin/access/team-members/{teamMemberId}/permissions/status", testTeamMember.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.TICKET.READ").value(true))
                .andExpect(jsonPath("$.TICKET.CREATE").value(false))
                .andExpect(jsonPath("$.TICKET.UPDATE").value(false))
                .andExpect(jsonPath("$.TICKET.DELETE").value(false))
                .andExpect(jsonPath("$.ACCESS.ADMIN.get").value(false))
                .andExpect(jsonPath("$.CUSTOMER.READ").value(false))
                .andExpect(jsonPath("$.TEAM_MEMBER.UPDATE").value(false))
                .andExpect(jsonPath("$.TEAM_MANAGER.READ").value(false))
                .andExpect(jsonPath("$.SLA.READ").value(false));
    }

    @Test
    @Order(5)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testGetTeamMemberPermissionStatus_AppliesDenyGrant() throws Exception {
        accessAdminService.upsertGrant(testTeamMember.getId(), "TICKET_READ", GrantEffect.DENY);

        mockMvc.perform(get("/api/admin/access/team-members/{teamMemberId}/permissions/status", testTeamMember.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.TICKET.READ").value(false));
    }

    @Test
    @Order(6)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testGetTeamMemberPermissionStatus_CustomerIdNotFound() throws Exception {
        mockMvc.perform(get("/api/admin/access/team-members/{teamMemberId}/permissions/status", testUser.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(7)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testGetEffectiveAccess_UserNotFound() throws Exception {
        mockMvc.perform(get("/api/admin/access/users/{userId}", 99999L))
                .andExpect(status().isNotFound());
    }

    // ==================== User Permission Grants Tests ====================

/*******************    💫 Codegeex Suggestion    *******************/
    @Test
    @Order(8)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testListGrants_EmptyInitially() throws Exception {
        mockMvc.perform(get("/api/admin/access/users/{userId}/grants", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
/****************  fb3be9ffd73b4215ad371eb391d7540d  ****************/

    @Test
    @Order(9)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testUpsertGrant_CreateNew() throws Exception {
        GrantDto grantDto = new GrantDto("TICKET_WRITE", GrantEffect.ALLOW);

        mockMvc.perform(post("/api/admin/access/users/{userId}/grants", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grantDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.permissionCode").value("TICKET_WRITE"))
                .andExpect(jsonPath("$.effect").value("ALLOW"));
    }

    @Test
    @Order(10)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testUpsertGrant_UpdateExisting() throws Exception {
        // First create a grant
        GrantDto allowGrant = new GrantDto("TICKET_WRITE", GrantEffect.ALLOW);
        mockMvc.perform(post("/api/admin/access/users/{userId}/grants", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(allowGrant)))
                .andExpect(status().isCreated());

        // Then update it to DENY
        GrantDto denyGrant = new GrantDto("TICKET_WRITE", GrantEffect.DENY);
        mockMvc.perform(post("/api/admin/access/users/{userId}/grants", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(denyGrant)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.permissionCode").value("TICKET_WRITE"))
                .andExpect(jsonPath("$.effect").value("DENY"));
    }

    @Test
    @Order(11)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testUpsertGrant_InvalidPermission() throws Exception {
        GrantDto grantDto = new GrantDto("INVALID_PERMISSION", GrantEffect.ALLOW);

        mockMvc.perform(post("/api/admin/access/users/{userId}/grants", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grantDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(12)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testRemoveGrant_Success() throws Exception {
        // First create a grant
        GrantDto grantDto = new GrantDto("TICKET_WRITE", GrantEffect.ALLOW);
        mockMvc.perform(post("/api/admin/access/users/{userId}/grants", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grantDto)))
                .andExpect(status().isCreated());

        // Then remove it
        mockMvc.perform(delete("/api/admin/access/users/{userId}/grants/{permissionCode}",
                        testUser.getId(), "TICKET_WRITE"))
                .andExpect(status().isNoContent());

        // Verify it's gone
        mockMvc.perform(get("/api/admin/access/users/{userId}/grants", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== User Scope Tests ====================

    @Test
    @Order(13)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testListScopes_EmptyInitially() throws Exception {
        mockMvc.perform(get("/api/admin/access/users/{userId}/scopes", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @Order(14)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testSetScope_CreateNew() throws Exception {
        ScopeUpdateDto scopeDto = new ScopeUpdateDto(AccessScope.ALL);

        mockMvc.perform(put("/api/admin/access/users/{userId}/scopes/{resourceType}",
                        testUser.getId(), "TICKET")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scopeDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceType").value("TICKET"))
                .andExpect(jsonPath("$.scope").value("ALL"));
    }

    @Test
    @Order(15)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testSetScope_UpdateExisting() throws Exception {
        // First set to ALL
        ScopeUpdateDto scopeAll = new ScopeUpdateDto(AccessScope.ALL);
        mockMvc.perform(put("/api/admin/access/users/{userId}/scopes/{resourceType}",
                        testUser.getId(), "TICKET")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scopeAll)))
                .andExpect(status().isOk());

        // Then update to OWN
        ScopeUpdateDto scopeOwn = new ScopeUpdateDto(AccessScope.OWN);
        mockMvc.perform(put("/api/admin/access/users/{userId}/scopes/{resourceType}",
                        testUser.getId(), "TICKET")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scopeOwn)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceType").value("TICKET"))
                .andExpect(jsonPath("$.scope").value("OWN"));
    }

    @Test
    @Order(16)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testClearScope_Success() throws Exception {
        // First set a scope
        ScopeUpdateDto scopeDto = new ScopeUpdateDto(AccessScope.ALL);
        mockMvc.perform(put("/api/admin/access/users/{userId}/scopes/{resourceType}",
                        testUser.getId(), "TICKET")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scopeDto)))
                .andExpect(status().isOk());

        // Then clear it
        mockMvc.perform(delete("/api/admin/access/users/{userId}/scopes/{resourceType}",
                        testUser.getId(), "TICKET"))
                .andExpect(status().isNoContent());

        // Verify it's gone
        mockMvc.perform(get("/api/admin/access/users/{userId}/scopes", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== Role Permissions Tests ====================

    @Test
    @Order(17)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testListRolePermissions_Success() throws Exception {
        mockMvc.perform(get("/api/admin/access/roles/{roleName}/permissions", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code").value("TICKET_READ"));
    }

    @Test
    @Order(18)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testListRolePermissions_RoleNotFound() throws Exception {
        mockMvc.perform(get("/api/admin/access/roles/{roleName}/permissions", "INVALID_ROLE"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(19)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testReplaceRolePermissions_Success() throws Exception {
        RolePermissionsUpdateDto updateDto = new RolePermissionsUpdateDto(
                Arrays.asList("TICKET_READ", "TICKET_WRITE")
        );

        mockMvc.perform(put("/api/admin/access/roles/{roleName}/permissions", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].code", hasItems("TICKET_READ", "TICKET_WRITE")));
    }

    @Test
    @Order(20)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testReplaceRolePermissions_WithInvalidPermission() throws Exception {
        RolePermissionsUpdateDto updateDto = new RolePermissionsUpdateDto(
                Arrays.asList("TICKET_READ", "INVALID_PERMISSION")
        );

        mockMvc.perform(put("/api/admin/access/roles/{roleName}/permissions", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(21)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testReplaceRolePermissions_EmptyList() throws Exception {
        RolePermissionsUpdateDto updateDto = new RolePermissionsUpdateDto(List.of());

        mockMvc.perform(put("/api/admin/access/roles/{roleName}/permissions", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== Validation Tests ====================

    @Test
    @Order(22)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testUpsertGrant_NullPermissionCode() throws Exception {
        String invalidJson = "{\"permissionCode\": null, \"effect\": \"ALLOW\"}";

        mockMvc.perform(post("/api/admin/access/users/{userId}/grants", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(23)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testUpsertGrant_NullEffect() throws Exception {
        String invalidJson = "{\"permissionCode\": \"TICKET_READ\", \"effect\": null}";

        mockMvc.perform(post("/api/admin/access/users/{userId}/grants", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(24)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testSetScope_NullScope() throws Exception {
        String invalidJson = "{\"scope\": null}";

        mockMvc.perform(put("/api/admin/access/users/{userId}/scopes/{resourceType}",
                        testUser.getId(), "TICKET")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    // ==================== Complex Scenario Tests ====================

    @Test
    @Order(25)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    @Transactional
    void testCompleteAccessManagementWorkflow() throws Exception {
        // 1. Check initial effective access
        mockMvc.perform(get("/api/admin/access/users/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissionCodes", hasItem("TICKET_READ")))
                .andExpect(jsonPath("$.permissionCodes", not(hasItem("TICKET_WRITE"))));

        // 2. Grant additional permission
        GrantDto grantDto = new GrantDto("TICKET_WRITE", GrantEffect.ALLOW);
        mockMvc.perform(post("/api/admin/access/users/{userId}/grants", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grantDto)))
                .andExpect(status().isCreated());

        // 3. Verify effective access includes new permission
        mockMvc.perform(get("/api/admin/access/users/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissionCodes", hasItems("TICKET_READ", "TICKET_WRITE")));

        // 4. Set custom scope
        ScopeUpdateDto scopeDto = new ScopeUpdateDto(AccessScope.ALL);
        mockMvc.perform(put("/api/admin/access/users/{userId}/scopes/{resourceType}",
                        testUser.getId(), "TICKET")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scopeDto)))
                .andExpect(status().isOk());

        // 5. Verify scope in effective access
        mockMvc.perform(get("/api/admin/access/users/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scopes.TICKET").value("ALL"));

        // 6. Deny a permission
        GrantDto denyDto = new GrantDto("TICKET_READ", GrantEffect.DENY);
        mockMvc.perform(post("/api/admin/access/users/{userId}/grants", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(denyDto)))
                .andExpect(status().isCreated());

        // 7. Verify DENY takes effect
        mockMvc.perform(get("/api/admin/access/users/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissionCodes", not(hasItem("TICKET_READ"))))
                .andExpect(jsonPath("$.permissionCodes", hasItem("TICKET_WRITE")));
    }

    @Test
    @Order(26)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testAssignPermissionToUser() throws Exception {
        mockMvc.perform(put("/api/admin/access/users/{userId}/permissions/{permissionCode}",
                        testUser.getId(), "ticket_write"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissionCode").value("TICKET_WRITE"))
                .andExpect(jsonPath("$.effect").value("ALLOW"));

        mockMvc.perform(get("/api/admin/access/users/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissionCodes", hasItem("TICKET_WRITE")));
    }

    @Test
    @Order(27)
    @WithMockUser(authorities = {"ACCESS_ADMIN"})
    void testRevokeRoleInheritedPermissionFromUser() throws Exception {
        mockMvc.perform(delete("/api/admin/access/users/{userId}/permissions/{permissionCode}",
                        testUser.getId(), "ticket_read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissionCode").value("TICKET_READ"))
                .andExpect(jsonPath("$.effect").value("DENY"));

        mockMvc.perform(get("/api/admin/access/users/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissionCodes", not(hasItem("TICKET_READ"))));
    }
}
