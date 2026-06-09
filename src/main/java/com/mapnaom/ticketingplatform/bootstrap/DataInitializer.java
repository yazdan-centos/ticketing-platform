package com.mapnaom.ticketingplatform.bootstrap;

import com.mapnaom.ticketingplatform.model.Customer;
import com.mapnaom.ticketingplatform.model.Permission;
import com.mapnaom.ticketingplatform.model.Role;
import com.mapnaom.ticketingplatform.model.SlaContract;
import com.mapnaom.ticketingplatform.model.TeamManager;
import com.mapnaom.ticketingplatform.model.TeamMember;
import com.mapnaom.ticketingplatform.model.Ticket;
import com.mapnaom.ticketingplatform.model.User;
import com.mapnaom.ticketingplatform.model.enums.Priority;
import com.mapnaom.ticketingplatform.model.enums.TicketStatus;
import com.mapnaom.ticketingplatform.repository.CustomerRepository;
import com.mapnaom.ticketingplatform.repository.PermissionRepository;
import com.mapnaom.ticketingplatform.repository.RoleRepository;
import com.mapnaom.ticketingplatform.repository.SlaContractRepository;
import com.mapnaom.ticketingplatform.repository.TeamManagerRepository;
import com.mapnaom.ticketingplatform.repository.TeamMemberRepository;
import com.mapnaom.ticketingplatform.repository.TicketRepository;
import com.mapnaom.ticketingplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Order(0)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final TeamManagerRepository teamManagerRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final SlaContractRepository slaContractRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedPermissionsIfEmpty();
        seedRolesIfEmpty();
        seedAccessUsersIfEmpty();
        seedAppUsersIfEmpty();
        seedSlaContractsIfEmpty();
        seedTicketsIfEmpty();
    }

    /**
     * Seeds the database with default permissions if the permission repository is currently empty. //t
     * <p>
     * This method checks the number of existing permissions in the repository. //r
     * If permissions already exist (count > 0), the method returns immediately without making any changes. //n
     * If the repository is empty, it populates it with a predefined list of default permissions
     * covering admin access, ticket management, customer data, team member records, and SLA contracts.
     * </p>
     */
    private void seedPermissionsIfEmpty() {
        if (permissionRepository.count() > 0) {
            return;
        }

        permissionRepository.saveAll(List.of(
                permission("ACCESS_ADMIN", "Manage access-control permissions and scopes"),
                permission("TICKET_CREATE", "Create support tickets"),
                permission("TICKET_READ", "Read support tickets"),
                permission("TICKET_UPDATE", "Update support tickets"),
                permission("TICKET_DELETE", "Delete support tickets"),
                permission("CUSTOMER_READ", "Read customer records"),
                permission("CUSTOMER_CREATE", "Create customer records"),
                permission("CUSTOMER_UPDATE", "Update customer records"),
                permission("CUSTOMER_DELETE", "Delete customer records"),
                permission("TEAM_MEMBER_READ", "Read team member records"),
                permission("TEAM_MEMBER_UPDATE", "Update team member records"),
                permission("TEAM_MANAGER_READ", "Read team manager records"),
                permission("SLA_READ", "Read SLA contracts"),
                permission("SLA_CREATE", "Create SLA contracts"),
                permission("SLA_UPDATE", "Update SLA contracts"),
                permission("SLA_DELETE", "Delete SLA contracts")));
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
    private Role getOrCreateRole(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(new Role(name))); // Assuming you have a Role constructor
    }

    private void seedAccessUsersIfEmpty() {
        if (userRepository.count() > 0) {
            return;
        }

        // This guarantees the roles exist, creating them if necessary
        Role customerRole = getOrCreateRole("CUSTOMER");
        Role teamMemberRole = getOrCreateRole("TEAM_MEMBER");
        Role teamManagerRole = getOrCreateRole("TEAM_MANAGER");

        userRepository.saveAll(List.of(
                accessUser("admin@example.com", "admin123", teamManagerRole),
                accessUser("member@example.com", "member123", teamMemberRole),
                accessUser("customer@example.com", "customer123", customerRole)));
    }

    private void seedAppUsersIfEmpty() {
        if (customerRepository.count() == 0) {
            List<Customer> customers = new java.util.ArrayList<>();
            for (int i = 1; i <= 20; i++) {
                customers.add(customer("customer" + i, "First" + i, "Last" + i, "customer" + i + "@example.com", "Company " + i));
            }
            customerRepository.saveAll(customers);
        }

        if (teamManagerRepository.count() == 0 && teamMemberRepository.count() == 0) {
            TeamManager manager = new TeamManager();
            manager.setUsername("manager");
            manager.setFirstName("Mina");
            manager.setLastName("Manager");
            manager.setEmail("manager@example.com");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setDepartment("Support");
            manager.setRoles(Set.of("TEAM_MANAGER"));

            manager.addTeamMember(teamMember("john", "John", "Jones", "john@example.com", "Backend Engineer"));
            manager.addTeamMember(teamMember("jane", "Jane", "James", "jane@example.com", "QA Engineer"));
            manager.addTeamMember(teamMember("sara", "Sara", "Smith", "sara@example.com", "DevOps Engineer"));

            teamManagerRepository.save(manager);
        }
    }

    private void seedSlaContractsIfEmpty() {
        if (slaContractRepository.count() > 0) {
            return;
        }

        List<Customer> customers = customerRepository.findAll();
        Customer firstCustomer = customers.isEmpty() ? null : customers.get(0);
        Customer secondCustomer = customers.size() > 1 ? customers.get(1) : firstCustomer;

        slaContractRepository.saveAll(List.of(
                slaContract("Critical 24/7 Support", "Critical production incidents", 1, true, firstCustomer),
                slaContract("Business Hours Support", "High priority business-hour requests", 4, true, secondCustomer),
                slaContract("Standard Support", "General support requests", 24, true, firstCustomer)));
    }

    private void seedTicketsIfEmpty() {
        if (ticketRepository.count() > 0) {
            return;
        }

        List<Customer> customers = customerRepository.findAll();
        List<TeamMember> members = teamMemberRepository.findAll();
        List<SlaContract> contracts = slaContractRepository.findAll();

        if (customers.isEmpty() || members.isEmpty() || contracts.isEmpty()) {
            return;
        }

        List<Ticket> tickets = new java.util.ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            TicketStatus status = (i % 2 == 0) ? TicketStatus.ASSIGNED : TicketStatus.IN_PROGRESS;
            Priority priority;
            if (i % 3 == 0) priority = Priority.CRITICAL;
            else if (i % 3 == 1) priority = Priority.MEDIUM;
            else priority = Priority.LOW;

            tickets.add(ticket("Ticket Issue " + i, "Description for ticket issue " + i,
                    status, priority,
                    customers.get((i - 1) % customers.size()),
                    members.get((i - 1) % members.size()),
                    contracts.get((i - 1) % contracts.size())));
        }
        ticketRepository.saveAll(tickets);
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

    private User accessUser(String email, String password, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Set.of(role));
        return user;
    }

    private Customer customer(String username, String firstName, String lastName, String email, String companyName) {
        Customer customer = new Customer();
        customer.setUsername(username);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customer.setCompanyName(companyName);
        customer.setPassword(passwordEncoder.encode(username + "123"));
        customer.setRoles(Set.of("CUSTOMER"));
        return customer;
    }

    private TeamMember teamMember(String username, String firstName, String lastName, String email, String jobTitle) {
        TeamMember teamMember = new TeamMember();
        teamMember.setUsername(username);
        teamMember.setFirstName(firstName);
        teamMember.setLastName(lastName);
        teamMember.setEmail(email);
        teamMember.setPassword(passwordEncoder.encode(username + "123"));
        teamMember.setJobTitle(jobTitle);
        teamMember.setRoles(Set.of("TEAM_MEMBER"));
        return teamMember;
    }

    private SlaContract slaContract(String name, String scope, Integer responseTimeHours, Boolean active, Customer customer) {
        SlaContract slaContract = new SlaContract();
        slaContract.setContractName(name);
        slaContract.setServiceScope(scope);
        slaContract.setResponseTimeHours(responseTimeHours);
        slaContract.setIsActive(active);
        slaContract.setCustomer(customer);
        return slaContract;
    }

    private Ticket ticket(String title,
                          String description,
                          TicketStatus status,
                          Priority priority,
                          Customer customer,
                          TeamMember assignedMember,
                          SlaContract slaContract) {
        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setStatus(status);
        ticket.setPriority(priority);
        ticket.setCustomer(customer);
        ticket.setAssignedMember(assignedMember);
        ticket.setSlaContract(slaContract);
        return ticket;
    }
}
