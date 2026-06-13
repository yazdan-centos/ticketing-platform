package com.mapnaom.ticketingplatform.bootstrap;

import com.mapnaom.ticketingplatform.model.Customer;
import com.mapnaom.ticketingplatform.model.Permission;
import com.mapnaom.ticketingplatform.model.Role;
import com.mapnaom.ticketingplatform.model.SlaContract;
import com.mapnaom.ticketingplatform.model.TeamManager;
import com.mapnaom.ticketingplatform.model.TeamMember;
import com.mapnaom.ticketingplatform.model.Ticket;
import com.mapnaom.ticketingplatform.model.enums.Priority;
import com.mapnaom.ticketingplatform.model.enums.TicketStatus;
import com.mapnaom.ticketingplatform.repository.CustomerRepository;
import com.mapnaom.ticketingplatform.repository.PermissionRepository;
import com.mapnaom.ticketingplatform.repository.RoleRepository;
import com.mapnaom.ticketingplatform.repository.SlaContractRepository;
import com.mapnaom.ticketingplatform.repository.TeamManagerRepository;
import com.mapnaom.ticketingplatform.repository.TeamMemberRepository;
import com.mapnaom.ticketingplatform.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public void run(String... args) {
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
     * the matching security {@link Role}. These are the accounts used to log in;
     * the JWT principal is the {@code AppUser} itself.
     *
     * <p>Demo credentials (login by username): {@code customer1..20 / customerN123},
     * {@code manager / manager123} (TEAM_MANAGER → also has ACCESS_ADMIN),
     * {@code john|jane|sara / <username>123} (TEAM_MEMBER).
     */
    private void seedAppUsersIfEmpty() {
        Role customerRole = getOrCreateRole("CUSTOMER");
        Role teamMemberRole = getOrCreateRole("TEAM_MEMBER");
        Role teamManagerRole = getOrCreateRole("TEAM_MANAGER");

        if (customerRepository.count() == 0) {
            List<Customer> customers = new java.util.ArrayList<>();
            for (int i = 1; i <= 20; i++) {
                customers.add(customer("customer" + i, "نام" + i, "نام‌خانوادگی" + i,
                        "customer" + i + "@example.com", "شرکت " + i, customerRole));
            }
            customerRepository.saveAll(customers);
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

    private void seedSlaContractsIfEmpty() {
        if (slaContractRepository.count() > 0) {
            return;
        }

        List<Customer> customers = customerRepository.findAll();
        Customer firstCustomer = customers.isEmpty() ? null : customers.get(0);
        Customer secondCustomer = customers.size() > 1 ? customers.get(1) : firstCustomer;

        slaContractRepository.saveAll(List.of(
                slaContract("پشتیبانی حیاتی 24/7", "حوادث حیاتی تولید", 1, true, firstCustomer),
                slaContract("پشتیبانی ساعات کاری", "درخواست‌های با اولویت بالا در ساعات کاری", 4, true, secondCustomer),
                slaContract("پشتیبانی استاندارد", "درخواست‌های پشتیبانی عمومی", 24, true, firstCustomer)));
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
        for (int i = 1; i <= 50; i++) {
            TicketStatus status = (i % 2 == 0) ? TicketStatus.ASSIGNED : TicketStatus.IN_PROGRESS;
            Priority priority;
            if (i % 3 == 0) priority = Priority.CRITICAL;
            else if (i % 3 == 1) priority = Priority.MEDIUM;
            else priority = Priority.LOW;

            tickets.add(ticket("مشکل تیکت " + i, "توضیحات برای مشکل تیکت " + i,
                    status, priority,
                    customers.get((i - 1) % customers.size()),
                    members.get((i - 1) % members.size()),
                    contracts.get((i - 1) % contracts.size())));
        }
        ticketRepository.saveAll(tickets);
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

    private Customer customer(String username, String firstName, String lastName,
                              String email, String companyName, Role role) {
        Customer customer = new Customer();
        customer.setUsername(username);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customer.setCompanyName(companyName);
        customer.setPassword(passwordEncoder.encode(username + "123"));
        customer.setRoles(Set.of(role));
        return customer;
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
