package com.mapnaom.ticketingplatform.bootstrap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mapnaom.ticketingplatform.model.Customer;
import com.mapnaom.ticketingplatform.model.Permission;
import com.mapnaom.ticketingplatform.model.Role;
import com.mapnaom.ticketingplatform.model.SlaContract;
import com.mapnaom.ticketingplatform.model.TeamManager;
import com.mapnaom.ticketingplatform.model.TeamMember;
import com.mapnaom.ticketingplatform.model.Ticket;
import com.mapnaom.ticketingplatform.model.enums.TicketStatus;
import com.mapnaom.ticketingplatform.repository.CustomerRepository;
import com.mapnaom.ticketingplatform.repository.PermissionRepository;
import com.mapnaom.ticketingplatform.repository.RoleRepository;
import com.mapnaom.ticketingplatform.repository.SlaContractRepository;
import com.mapnaom.ticketingplatform.repository.TeamManagerRepository;
import com.mapnaom.ticketingplatform.repository.TeamMemberRepository;
import com.mapnaom.ticketingplatform.repository.TicketRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Order(0)
@Profile("!test") // Demo data must not seed into the test database; integration tests set up their own fixtures.
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final TeamManagerRepository teamManagerRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final SlaContractRepository slaContractRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    @Transactional
    public void run(String... args) throws IOException {
        seedPermissionsIfEmpty();
        seedRolesIfEmpty();
        seedAppUsersIfEmpty();
        seedSlaContractsIfEmpty();
        seedTicketsIfEmpty();
    }

    private void seedPermissionsIfEmpty() {
        if (permissionRepository.count() > 0) {
            return;
        }

        permissionRepository.saveAll(List.of(
                permission("ACCESS_ADMIN", "مدیریت دسترسی‌ها و محدوده‌ها"),
                permission("TICKET_CREATE", "ایجاد تیکت پشتیبانی"),
                permission("TICKET_READ", "مشاهده تیکت‌های پشتیبانی"),
                permission("TICKET_UPDATE", "به‌روزرسانی تیکت‌های پشتیبانی"),
                permission("TICKET_DELETE", "حذف تیکت‌های پشتیبانی"),
                permission("CUSTOMER_READ", "مشاهده سوابق مشتریان"),
                permission("CUSTOMER_CREATE", "ایجاد سوابق مشتریان"),
                permission("CUSTOMER_UPDATE", "به‌روزرسانی سوابق مشتریان"),
                permission("CUSTOMER_DELETE", "حذف سوابق مشتریان"),
                permission("TEAM_MEMBER_READ", "مشاهده سوابق اعضای تیم"),
                permission("TEAM_MEMBER_UPDATE", "به‌روزرسانی سوابق اعضای تیم"),
                permission("TEAM_MANAGER_READ", "مشاهده سوابق مدیران تیم"),
                permission("SLA_READ", "مشاهده قراردادهای SLA"),
                permission("SLA_CREATE", "ایجاد قراردادهای SLA"),
                permission("SLA_UPDATE", "به‌روزرسانی قراردادهای SLA"),
                permission("SLA_DELETE", "حذف قراردادهای SLA")));
    }

    private void seedRolesIfEmpty() {
        if (roleRepository.count() > 0) {
            return;
        }

        Set<Permission> allPermissions = Set.copyOf(permissionRepository.findAll());
        Role customer = role("CUSTOMER", "TICKET_CREATE", "TICKET_READ", "SLA_READ");
        Role teamMember = role("TEAM_MEMBER", "TICKET_READ", "TICKET_UPDATE", "CUSTOMER_READ", "SLA_READ");
        Role teamManager = new Role();

        teamManager.setName("TEAM_MANAGER");
        teamManager.setPermissions(allPermissions);

        roleRepository.saveAll(List.of(customer, teamMember, teamManager));
    }

    /**
     * Seeds the domain users (customers, a manager and members) and assigns each
     * the matching security {@link Role}. Customers now come from the bundled
     * {@code data/customers_seed_data.json} fixture instead of being generated
     * inline; the manager/members are still hardcoded since no fixture exists
     * for them yet.
     *
     * <p>Demo credentials (login by username): customer usernames/passwords as
     * defined in the JSON fixture; {@code manager / manager123} (TEAM_MANAGER →
     * also has ACCESS_ADMIN); {@code john|jane|sara / <username>123} (TEAM_MEMBER).
     */
    private void seedAppUsersIfEmpty() throws IOException {
        Role customerRole = getOrCreateRole("CUSTOMER");
        Role teamMemberRole = getOrCreateRole("TEAM_MEMBER");
        Role teamManagerRole = getOrCreateRole("TEAM_MANAGER");

        if (customerRepository.count() == 0) {
            seedCustomersFromJson(customerRole);
        }

        if (teamManagerRepository.count() == 0 && teamMemberRepository.count() == 0) {
            TeamManager manager = new TeamManager();
            manager.setUsername("manager");
            manager.setFirstName("مینا");
            manager.setLastName("مدیر");
            manager.setEmail("manager@example.com");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setDepartment("پشتیبانی");
            manager.setRoles(Set.of(teamManagerRole));

            manager.addTeamMember(teamMember("john", "جان", "جونز", "john@example.com", "مهندس بک‌اند", teamMemberRole));
            manager.addTeamMember(teamMember("jane", "جین", "جیمز", "jane@example.com", "مهندس تضمین کیفیت", teamMemberRole));
            manager.addTeamMember(teamMember("sara", "سارا", "اسمیت", "sara@example.com", "مهندس دواپس", teamMemberRole));

            teamManagerRepository.save(manager);
        }
    }

    /**
     * Loads {@code data/customers_seed_data.json} and persists one {@link Customer}
     * per entry. The fixture's {@code roles} field doesn't map to this project's
     * Role taxonomy (CUSTOMER / TEAM_MEMBER / TEAM_MANAGER), so it's ignored —
     * every seeded customer simply gets the CUSTOMER role.
     */
    private void seedCustomersFromJson(Role customerRole) throws IOException {
        List<CustomerSeedDto> seeds = readJsonArray("/data/customers_seed_data.json", CustomerSeedDto.class);

        List<Customer> customers = new ArrayList<>();
        for (CustomerSeedDto seed : seeds) {
            Customer customer = new Customer();
            customer.setUsername(seed.getUsername());
            customer.setPassword(passwordEncoder.encode(seed.getPassword()));
            customer.setEmail(seed.getEmail());
            customer.setCompanyName(seed.getCompanyName());
            customer.setPhone(seed.getPhone());
            customer.setRoles(Set.of(customerRole));
            customers.add(customer);
        }
        customerRepository.saveAll(customers);
    }

    /**
     * Loads {@code data/sla_contracts_seed_data_persian.json}. Each entry's nested
     * {@code customer.id} is a 1-based position into the customers fixture
     * (not a database id), so it's resolved positionally against the freshly
     * saved customer list. {@code createdAt}/{@code updatedAt} from the JSON
     * are not applied —  stamps them itself.
     */
    private void seedSlaContractsIfEmpty() throws IOException {
        if (slaContractRepository.count() > 0) {
            return;
        }

        List<SlaContractSeedDto> seeds = readJsonArray("/data/sla_contracts_seed_data_persian.json", SlaContractSeedDto.class);
        List<Customer> customers = customerRepository.findAll();

        List<SlaContract> contracts = new ArrayList<>();
        for (SlaContractSeedDto seed : seeds) {
            Long position = seed.getCustomer() != null ? seed.getCustomer().getId() : null;
            Customer customer = resolveByPosition(customers, position);

            contracts.add(SlaContract.builder()
                    .contractName(seed.getContractName())
                    .serviceScope(seed.getServiceScope())
                    .responseTimeHours(seed.getResponseTimeHours())
                    .isActive(seed.getIsActive())
                    .customer(customer)
                    .build());
        }
        slaContractRepository.saveAll(contracts);
    }

    /**
     * Loads {@code data/ticket_seed_data.json}. {@code customerId} and
     * {@code slaContractId} are resolved positionally (same convention as the
     * contracts fixture). {@code assignedMemberId} ranges 1-4 in the fixture,
     * but only 3 {@link TeamMember}s are seeded, so it wraps with modulo
     * rather than failing.
     */
    private void seedTicketsIfEmpty() throws IOException {
        if (ticketRepository.count() > 0) {
            return;
        }

        List<TicketSeedDto> seeds = readJsonArray("/data/ticket_seed_data.json", TicketSeedDto.class);
        List<Customer> customers = customerRepository.findAll();
        List<SlaContract> contracts = slaContractRepository.findAll();
        List<TeamMember> members = teamMemberRepository.findAll();

        if (customers.isEmpty()) {
            return;
        }

        List<Ticket> tickets = new ArrayList<>();
        for (TicketSeedDto seed : seeds) {
            Customer customer = resolveByPosition(customers, seed.getCustomerId());
            if (customer == null) {
                continue;
            }

            SlaContract slaContract = resolveByPosition(contracts, seed.getSlaContractId());

            TeamMember assignedMember = null;
            if (seed.getAssignedMemberId() != null && !members.isEmpty()) {
                int index = (int) ((seed.getAssignedMemberId() - 1) % members.size());
                assignedMember = members.get(index);
            }

            Ticket ticket = new Ticket();
            ticket.setTitle(seed.getTitle());
            ticket.setDescription(seed.getDescription());
            ticket.setCustomer(customer);
            ticket.setSlaContract(slaContract);
            ticket.setAssignedMember(assignedMember);
            ticket.setStatus(assignedMember != null ? TicketStatus.ASSIGNED : TicketStatus.UNALLOCATED);
            tickets.add(ticket);
        }
        ticketRepository.saveAll(tickets);
    }

    private <T> T resolveByPosition(List<T> items, Long position) {
        if (position == null || position < 1 || position > items.size()) {
            return null;
        }
        return items.get((int) (position - 1));
    }

    private <T> List<T> readJsonArray(String classpathLocation, Class<T> elementType) throws IOException {
        try (InputStream inputStream = new ClassPathResource(classpathLocation).getInputStream()) {
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, elementType);
            return objectMapper.readValue(inputStream, listType);
        }
    }

    private Role getOrCreateRole(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(new Role(name)));
    }

    private Permission permission(String code, String description) {
        Permission permission = new Permission();
        permission.setCode(code);
        permission.setDescription(description);
        return permission;
    }

    private Role role(String name, String... permissionCodes) {
        Set<String> requestedCodes = Set.of(permissionCodes);
        Set<Permission> permissions = permissionRepository.findAll().stream()
                .filter(permission -> requestedCodes.contains(permission.getCode()))
                .collect(Collectors.toSet());

        Role role = new Role();
        role.setName(name);
        role.setPermissions(permissions);
        return role;
    }

    private TeamMember teamMember(String username, String firstName, String lastName,
                                  String email, String jobTitle, Role role) {
        TeamMember teamMember = new TeamMember();
        teamMember.setUsername(username);
        teamMember.setFirstName(firstName);
        teamMember.setLastName(lastName);
        teamMember.setEmail(email);
        teamMember.setPassword(passwordEncoder.encode(username + "123"));
        teamMember.setJobTitle(jobTitle);
        teamMember.setRoles(Set.of(role));
        return teamMember;
    }

    // ---- Lightweight DTOs matching the seed JSON shape (kept private to this class) ----

    @Data
    private static class CustomerSeedDto {
        private String username;
        private String password;
        private String email;
        private Set<String> roles; // not used - this project's Role taxonomy doesn't match
        private String companyName;
        private String phone;
    }

    @Data
    private static class SlaContractSeedDto {
        private Long id;
        private String contractName;
        private String serviceScope;
        private Integer responseTimeHours;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private CustomerRefSeedDto customer;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class CustomerRefSeedDto {
        private Long id;
    }

    @Data
    private static class TicketSeedDto {
        private String title;
        private String description;
        private Long customerId;
        private Long slaContractId;
        private Long assignedMemberId;
    }
}
