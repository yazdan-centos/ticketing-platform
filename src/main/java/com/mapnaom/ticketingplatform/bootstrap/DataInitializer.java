package com.mapnaom.ticketingplatform.bootstrap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mapnaom.ticketingplatform.model.Customer;
import com.mapnaom.ticketingplatform.model.AgendaItem;
import com.mapnaom.ticketingplatform.model.AppUser;
import com.mapnaom.ticketingplatform.model.Meeting;
import com.mapnaom.ticketingplatform.model.MeetingNote;
import com.mapnaom.ticketingplatform.model.MeetingParticipant;
import com.mapnaom.ticketingplatform.model.Permission;
import com.mapnaom.ticketingplatform.model.Role;
import com.mapnaom.ticketingplatform.model.SlaContract;
import com.mapnaom.ticketingplatform.model.Task;
import com.mapnaom.ticketingplatform.model.Team;
import com.mapnaom.ticketingplatform.model.TeamManager;
import com.mapnaom.ticketingplatform.model.TeamMember;
import com.mapnaom.ticketingplatform.model.TeamMembership;
import com.mapnaom.ticketingplatform.model.Ticket;
import com.mapnaom.ticketingplatform.model.enums.MeetingStatus;
import com.mapnaom.ticketingplatform.model.enums.NoteType;
import com.mapnaom.ticketingplatform.model.enums.Priority;
import com.mapnaom.ticketingplatform.model.enums.RsvpStatus;
import com.mapnaom.ticketingplatform.model.enums.TaskStatus;
import com.mapnaom.ticketingplatform.model.enums.TeamRole;
import com.mapnaom.ticketingplatform.model.enums.TicketStatus;
import com.mapnaom.ticketingplatform.repository.AppUserRepository;
import com.mapnaom.ticketingplatform.repository.CustomerRepository;
import com.mapnaom.ticketingplatform.repository.MeetingRepository;
import com.mapnaom.ticketingplatform.repository.PermissionRepository;
import com.mapnaom.ticketingplatform.repository.RoleRepository;
import com.mapnaom.ticketingplatform.repository.SlaContractRepository;
import com.mapnaom.ticketingplatform.repository.TaskRepository;
import com.mapnaom.ticketingplatform.repository.TeamMembershipRepository;
import com.mapnaom.ticketingplatform.repository.TeamManagerRepository;
import com.mapnaom.ticketingplatform.repository.TeamMemberRepository;
import com.mapnaom.ticketingplatform.repository.TeamRepository;
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Order(0)
@Profile("!test") // Demo data must not seed into the test database; integration tests set up their own fixtures.
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String DEFAULT_TEAM_MEMBER_PASSWORD = "password123";

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final AppUserRepository appUserRepository;
    private final CustomerRepository customerRepository;
    private final TeamManagerRepository teamManagerRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final SlaContractRepository slaContractRepository;
    private final TicketRepository ticketRepository;
    private final TeamRepository teamRepository;
    private final TeamMembershipRepository teamMembershipRepository;
    private final MeetingRepository meetingRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    @Transactional
    public void run(String... args) throws IOException {
        Set<String> newPermissionCodes = syncPermissions();
        seedRolesIfEmpty();
        assignNewPermissionsToDefaultRoles(newPermissionCodes);
        seedAppUsersIfEmpty();
        seedSlaContractsIfEmpty();
        seedTicketsIfEmpty();
        seedTeamsIfEmpty();
        seedMeetingsIfEmpty();
        seedMeetingTasksIfEmpty();
    }

/*******************    💫 Codegeex Inline Diff    *******************/
    private Set<String> syncPermissions() {
        List<Permission> definedPermissions = List.of(
                permission("ACCESS_ADMIN", "مدیریت دسترسی‌ها و محدوده‌ها"),
                permission("USER_CREATE", "ایجاد کاربر"),
                permission("USER_READ", "مشاهده کاربران"),
                permission("USER_UPDATE", "ویرایش کاربران و نقش‌های آن‌ها"),
                permission("USER_DELETE", "حذف کاربران بدون داده وابسته"),
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
                permission("TEAM_CREATE", "ایجاد تیم"),
                permission("TEAM_READ", "مشاهده تیم‌ها"),
                permission("TEAM_UPDATE", "به‌روزرسانی تیم‌ها و اعضای آن‌ها"),
                permission("TEAM_DELETE", "حذف تیم‌ها"),
                permission("MEETING_CREATE", "ایجاد جلسه"),
                permission("MEETING_READ", "مشاهده جلسات"),
                permission("MEETING_UPDATE", "به‌روزرسانی جلسات، شرکت‌کنندگان، دستور جلسه و یادداشت‌ها"),
                permission("MEETING_DELETE", "حذف جلسات"),
                permission("TASK_CREATE", "ایجاد وظیفه"),
                permission("TASK_READ", "مشاهده وظایف"),
                permission("TASK_UPDATE", "به‌روزرسانی وظایف"),
                permission("TASK_DELETE", "حذف وظایف"),
                permission("SLA_READ", "مشاهده قراردادهای SLA"),
                permission("SLA_CREATE", "ایجاد قراردادهای SLA"),
                permission("SLA_UPDATE", "به‌روزرسانی قراردادهای SLA"),
                permission("SLA_DELETE", "حذف قراردادهای SLA"));

        Map<String, Permission> existingPermissions = permissionRepository.findAll().stream()
                .collect(Collectors.toMap(Permission::getCode, Function.identity()));
        Set<String> newPermissionCodes = definedPermissions.stream()
                .map(Permission::getCode)
                .filter(code -> !existingPermissions.containsKey(code))
                .collect(Collectors.toSet());

        List<Permission> synchronizedPermissions = definedPermissions.stream()
                .map(definedPermission -> {
                    Permission existingPermission = existingPermissions.get(definedPermission.getCode());
                    if (existingPermission == null) {
                        return definedPermission;
                    }
                    existingPermission.setDescription(definedPermission.getDescription());
                    return existingPermission;
                })
                .toList();

        permissionRepository.saveAll(synchronizedPermissions);
        return newPermissionCodes;
    }
/****************  5dd8e72be2ce4a34a3e69226bd05e106  ****************/

    private void seedRolesIfEmpty() {
        if (roleRepository.count() > 0) {
            return;
        }

        Set<Permission> allPermissions = Set.copyOf(permissionRepository.findAll());
        Role customer = role("CUSTOMER", "TICKET_CREATE", "TICKET_READ", "SLA_READ");
        Role teamMember = role(
                "TEAM_MEMBER",
                "TICKET_READ", "TICKET_UPDATE", "CUSTOMER_READ", "SLA_READ",
                "TEAM_READ",
                "MEETING_CREATE", "MEETING_READ", "MEETING_UPDATE",
                "TASK_READ", "TASK_UPDATE");
        Role teamManager = new Role();

        teamManager.setName("TEAM_MANAGER");
        teamManager.setPermissions(new HashSet<>(allPermissions));

        roleRepository.saveAll(List.of(customer, teamMember, teamManager));
    }

    private void assignNewPermissionsToDefaultRoles(Set<String> newPermissionCodes) {
        if (newPermissionCodes.isEmpty()) {
            return;
        }

        Map<String, Permission> permissionsByCode = permissionRepository.findAll().stream()
                .collect(Collectors.toMap(Permission::getCode, Function.identity()));

        addRolePermissions("TEAM_MEMBER", newPermissionCodes, permissionsByCode, Set.of(
                "TEAM_READ",
                "MEETING_CREATE", "MEETING_READ", "MEETING_UPDATE",
                "TASK_READ", "TASK_UPDATE"));
        addRolePermissions("TEAM_MANAGER", newPermissionCodes, permissionsByCode, newPermissionCodes);
    }

    private void addRolePermissions(String roleName,
                                    Set<String> newPermissionCodes,
                                    Map<String, Permission> permissionsByCode,
                                    Set<String> defaultPermissionCodes) {
        roleRepository.findByName(roleName).ifPresent(role -> {
            Set<Permission> permissions = new HashSet<>(role.getPermissions());
            defaultPermissionCodes.stream()
                    .filter(newPermissionCodes::contains)
                    .map(permissionsByCode::get)
                    .filter(Objects::nonNull)
                    .forEach(permissions::add);
            role.setPermissions(permissions);
            roleRepository.save(role);
        });
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
     * also has ACCESS_ADMIN); all team members use {@code password123}.
     */
    private void seedAppUsersIfEmpty() throws IOException {
        Role customerRole = getOrCreateRole("CUSTOMER");
        Role teamMemberRole = getOrCreateRole("TEAM_MEMBER");
        Role teamManagerRole = getOrCreateRole("TEAM_MANAGER");

        if (customerRepository.count() == 0) {
            seedCustomersFromJson(customerRole);
        }

        TeamManager manager = null;
        if (!appUserRepository.existsByUsernameIgnoreCase("manager")) {
            manager = new TeamManager();
            manager.setUsername("manager");
            manager.setFirstName("مینا");
            manager.setLastName("مدیر");
            manager.setEmail("manager@example.com");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setDepartment("پشتیبانی");
            manager.setRoles(Set.of(teamManagerRole));
        } else {
            manager = teamManagerRepository.findAll().stream()
                    .filter(existingManager -> "manager".equalsIgnoreCase(existingManager.getUsername()))
                    .findFirst()
                    .orElse(null);
        }

        if (manager != null) {
            seedTeamMemberIfMissing(manager, "yazdanparast_m", "مهدی", "یزدان پرست", "sara@mps.mapnagroup.com", "برنامه نویس", teamMemberRole);
            seedTeamMemberIfMissing(manager, "aghelifar", "مهرنوش", "عاقلی فر", "aghelifar_m@mps.mapnagroup.com", "برنامه نویس", teamMemberRole);
            seedTeamMemberIfMissing(manager, "Nematollahian_m", "محمد", "نعمت الهیان", "Nematollahian_m@mps.mapnagroup.com", "برنامه نویس", teamMemberRole);
            seedTeamMemberIfMissing(manager, "Rahmani_mh", "فرشاد رحمانی", "محمد حسین", "sara@mps.mapnagroup.com", "برنامه نویس", teamMemberRole);
            seedTeamMemberIfMissing(manager, "naji_smh", "سید محمد حسن", "ناجی", "sara@mps.mapnagroup.com", "برنامه نویس", teamMemberRole);
            seedTeamMemberIfMissing(manager, "Motaghian_m", "ملیگا", "اسمیت", "motaghian_m@mps.mapnagroup.com", "برنامه نویس", teamMemberRole);
            seedTeamMemberIfMissing(manager, "Gordani_ma", "محمد امین", "گردانی","Gordani_ma@mps.mapnagroup.com", "برنامه نویس", teamMemberRole);
            seedTeamMemberIfMissing(manager, "Bagherpour_a", "امیر", "باقرپور", "sara@mps.mapnagroup.com", "برنامه نویس", teamMemberRole);
            teamManagerRepository.save(manager);
        }
    }

    private void seedTeamMemberIfMissing(TeamManager manager, String username, String firstName,
                                         String lastName, String email, String jobTitle, Role role) {
        if (appUserRepository.existsByUsernameIgnoreCase(username)) {
            return;
        }

        manager.addTeamMember(teamMember(username, firstName, lastName, email, jobTitle, role));
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

    private void seedTeamsIfEmpty() {
        if (teamRepository.count() > 0) {
            return;
        }

        List<TeamMember> members = sortedTeamMembers();
        TeamManager manager = teamManagerRepository.findAll().stream().findFirst().orElse(null);
        if (manager == null || members.isEmpty()) {
            return;
        }

        Team platform = team("توسعه پلتفرم", "توسعه هسته سامانه، معماری و بهبود تجربه توسعه‌دهندگان");
        Team operations = team("عملیات و پایداری", "پایش سرویس‌ها، مدیریت رخدادها و برنامه‌ریزی ظرفیت");
        Team support = team("تحویل و پشتیبانی", "هماهنگی تحویل، رسیدگی به مشتریان و برنامه‌ریزی پشتیبانی");
        teamRepository.saveAll(List.of(platform, operations, support));

        List<TeamMembership> memberships = new ArrayList<>();
        memberships.add(teamMembership(platform, manager, TeamRole.LEAD));
        memberships.add(teamMembership(operations, manager, TeamRole.LEAD));
        memberships.add(teamMembership(support, manager, TeamRole.LEAD));

        for (int index = 0; index < members.size(); index++) {
            TeamMember member = members.get(index);
            memberships.add(teamMembership(index < 5 ? platform : operations, member, TeamRole.MEMBER));
            if (index % 2 == 0) {
                memberships.add(teamMembership(support, member, TeamRole.MEMBER));
            }
        }
        teamMembershipRepository.saveAll(memberships);
    }

    private void seedMeetingsIfEmpty() {
        if (meetingRepository.count() > 0) {
            return;
        }

        List<Team> teams = teamRepository.findAll().stream()
                .filter(Team::isActive)
                .sorted(Comparator.comparing(Team::getId))
                .toList();
        List<TeamMember> members = sortedTeamMembers();
        TeamManager manager = teamManagerRepository.findAll().stream().findFirst().orElse(null);
        if (teams.size() < 3 || manager == null || members.size() < 6) {
            return;
        }

        LocalDateTime baseTime = LocalDateTime.now().withSecond(0).withNano(0);
        List<Meeting> meetings = new ArrayList<>();

        meetings.add(meeting(
                teams.get(0), manager, "برنامه‌ریزی اسپرینت", "تعیین هدف‌ها و ظرفیت اسپرینت بعدی",
                baseTime.plusDays(2).withHour(9).withMinute(30), 90, "اتاق جلسات البرز",
                MeetingStatus.SCHEDULED, List.of(members.get(0), members.get(1), members.get(2), members.get(3)),
                List.of("مرور ظرفیت تیم", "اولویت‌بندی بک‌لاگ", "تعیین هدف اسپرینت"), null));

        meetings.add(meeting(
                teams.get(0), members.get(0), "بازبینی معماری سرویس جلسات", "مرور مدل داده و مرزهای سرویس جدید",
                baseTime.plusDays(5).withHour(11).withMinute(0), 75, "https://meet.example.com/architecture",
                MeetingStatus.SCHEDULED, List.of(manager, members.get(1), members.get(2), members.get(4)),
                List.of("مرور طراحی دامنه", "بررسی امنیت و مجوزها", "تصمیم درباره انتشار"),
                "نسخه اولیه طراحی برای بازبینی آماده شده است."));

        meetings.add(meeting(
                teams.get(0), members.get(1), "بازنگری اسپرینت گذشته", "بررسی خروجی‌ها، موانع و اقدامات اصلاحی",
                baseTime.minusDays(2).withHour(14).withMinute(0), 60, "اتاق جلسات دماوند",
                MeetingStatus.COMPLETED, List.of(manager, members.get(0), members.get(2), members.get(3)),
                List.of("دموی قابلیت‌های تکمیل‌شده", "مرور شاخص‌های اسپرینت", "اقدامات بهبود"),
                "تصمیم شد بازبینی کد برای تغییرات حساس الزامی باشد."));

        meetings.add(meeting(
                teams.get(1), manager, "بازبینی رخداد سرویس", "تحلیل ریشه‌ای قطعی اخیر و اقدامات پیشگیرانه",
                baseTime.minusDays(1).withHour(10).withMinute(0), 75, "اتاق وضعیت",
                MeetingStatus.COMPLETED, List.of(members.get(5), members.get(6), members.get(7)),
                List.of("خط زمانی رخداد", "تحلیل علت ریشه‌ای", "اقدامات پیشگیرانه"),
                "هشدار مصرف حافظه باید پیش از رسیدن به آستانه بحرانی فعال شود."));

        meetings.add(meeting(
                teams.get(1), members.get(5), "تحویل شیفت آنکال", "انتقال وضعیت سرویس‌ها و رخدادهای باز",
                baseTime.plusDays(1).withHour(8).withMinute(30), 30, "کانال عملیات",
                MeetingStatus.SCHEDULED, List.of(manager, members.get(6), members.get(7)),
                List.of("رخدادهای باز", "تغییرات برنامه‌ریزی‌شده", "ریسک‌های شیفت بعد"), null));

        meetings.add(meeting(
                teams.get(1), manager, "برنامه‌ریزی ظرفیت فصل آینده", "پیش‌بینی رشد بار و نیازهای زیرساختی",
                baseTime.plusDays(7).withHour(13).withMinute(0), 90, "اتاق جلسات زاگرس",
                MeetingStatus.SCHEDULED, List.of(members.get(5), members.get(6), members.get(7)),
                List.of("روند مصرف منابع", "پیش‌بینی رشد", "بودجه زیرساخت"), null));

        meetings.add(meeting(
                teams.get(2), manager, "بررسی درخواست‌های بحرانی مشتریان", "مرور پرونده‌های مهم و هماهنگی پاسخ",
                baseTime.plusDays(3).withHour(10).withMinute(30), 60, "اتاق پشتیبانی",
                MeetingStatus.SCHEDULED, List.of(members.get(0), members.get(2), members.get(4), members.get(6)),
                List.of("پرونده‌های اولویت بالا", "تعهدات SLA", "مسئول هر پیگیری"), null));

        meetings.add(meeting(
                teams.get(2), manager, "نشست ماهانه همه اعضا", "اشتراک وضعیت محصول، عملیات و بازخورد مشتریان",
                baseTime.plusDays(10).withHour(15).withMinute(0), 90, "سالن اجتماعات",
                MeetingStatus.SCHEDULED, members,
                List.of("گزارش محصول", "گزارش پایداری", "بازخورد مشتریان", "پرسش و پاسخ"),
                "هر تیم یک گزارش پنج دقیقه‌ای آماده کند."));

        meetings.add(meeting(
                teams.get(2), members.get(2), "هماهنگی ارائه‌دهنده ویدئو", "بررسی کیفیت و ظرفیت سرویس جلسه آنلاین",
                baseTime.plusDays(4).withHour(16).withMinute(0), 45, "https://meet.example.com/vendor-sync",
                MeetingStatus.CANCELLED, List.of(manager, members.get(0), members.get(4)),
                List.of("کیفیت تماس", "ظرفیت همزمان", "برنامه ارتقا"),
                "جلسه به درخواست ارائه‌دهنده لغو شد."));

        meetings.add(meeting(
                teams.get(1), members.get(6), "هماهنگی روزانه عملیات", "مرور سریع سلامت سرویس‌ها و موانع جاری",
                baseTime.minusMinutes(15), 45, "کانال عملیات",
                MeetingStatus.IN_PROGRESS, List.of(manager, members.get(5), members.get(7)),
                List.of("سلامت سرویس‌ها", "هشدارهای فعال", "موانع امروز"), null));

        meetingRepository.saveAll(meetings);
    }

    private void seedMeetingTasksIfEmpty() {
        boolean meetingTasksExist = taskRepository.findAll().stream().anyMatch(task -> task.getMeeting() != null);
        if (meetingTasksExist) {
            return;
        }

        List<Meeting> meetings = meetingRepository.findAll().stream()
                .filter(Meeting::isActive)
                .sorted(Comparator.comparing(Meeting::getStartTime))
                .toList();
        List<TeamMember> members = sortedTeamMembers();
        TeamManager manager = teamManagerRepository.findAll().stream().findFirst().orElse(null);
        if (meetings.size() < 8 || members.size() < 8 || manager == null) {
            return;
        }

        Map<String, Meeting> meetingsByTitle = meetings.stream()
                .collect(Collectors.toMap(Meeting::getTitle, Function.identity()));
        Meeting incidentReview = meetingsByTitle.get("بازبینی رخداد سرویس");
        Meeting retrospective = meetingsByTitle.get("بازنگری اسپرینت گذشته");
        Meeting onCallHandover = meetingsByTitle.get("تحویل شیفت آنکال");
        Meeting sprintPlanning = meetingsByTitle.get("برنامه‌ریزی اسپرینت");
        Meeting capacityPlanning = meetingsByTitle.get("برنامه‌ریزی ظرفیت فصل آینده");
        Meeting customerEscalations = meetingsByTitle.get("بررسی درخواست‌های بحرانی مشتریان");
        Meeting monthlyAllHands = meetingsByTitle.get("نشست ماهانه همه اعضا");
        Meeting cancelledVendorSync = meetingsByTitle.get("هماهنگی ارائه‌دهنده ویدئو");
        Meeting operationsStandup = meetingsByTitle.get("هماهنگی روزانه عملیات");
        if (java.util.stream.Stream.of(incidentReview, retrospective, onCallHandover, sprintPlanning,
                capacityPlanning, customerEscalations, monthlyAllHands, cancelledVendorSync, operationsStandup)
                .anyMatch(Objects::isNull)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        List<Task> tasks = List.of(
                task("مستندسازی علت ریشه‌ای رخداد", "گزارش RCA و خط زمانی رخداد تکمیل شود.", incidentReview, members.get(5), manager, TaskStatus.COMPLETED, Priority.HIGH, 100, now.minusHours(6), now.minusDays(1)),
                task("افزودن هشدار مصرف حافظه", "هشدارهای مرحله‌ای برای سرویس‌های اصلی تعریف شود.", incidentReview, members.get(6), manager, TaskStatus.IN_PROGRESS, Priority.HIGH, 65, now.plusDays(1), null),
                task("بازنگری چک‌لیست انتشار", "موارد کنترل سلامت پس از انتشار به چک‌لیست اضافه شود.", incidentReview, members.get(7), manager, TaskStatus.OPEN, Priority.MEDIUM, 10, now.plusDays(3), null),
                task("ثبت اقدامات بهبود اسپرینت", "اقدامات توافق‌شده در برد تیم ثبت و اولویت‌بندی شود.", retrospective, members.get(1), manager, TaskStatus.COMPLETED, Priority.MEDIUM, 100, now.minusHours(4), now.minusDays(1)),
                task("الزام بازبینی تغییرات حساس", "قانون بازبینی دو نفره برای ماژول‌های حساس اعمال شود.", retrospective, members.get(0), manager, TaskStatus.IN_PROGRESS, Priority.HIGH, 45, now.plusDays(2), null),
                task("تکمیل وضعیت رخدادهای باز", "تمام رخدادهای باز پیش از تحویل شیفت به‌روزرسانی شوند.", onCallHandover, members.get(5), manager, TaskStatus.PENDING, Priority.HIGH, 0, now.plusHours(18), null),
                task("آماده‌سازی ظرفیت اعضای تیم", "ظرفیت تعطیلات و کارهای پشتیبانی در برنامه لحاظ شود.", sprintPlanning, members.get(2), manager, TaskStatus.IN_PROGRESS, Priority.MEDIUM, 35, now.plusDays(1), null),
                task("پاک‌سازی بک‌لاگ اسپرینت", "آیتم‌های منسوخ بسته و موارد مبهم برای پالایش مشخص شوند.", sprintPlanning, members.get(3), manager, TaskStatus.OPEN, Priority.LOW, 15, now.plusDays(1), null),
                task("تهیه پیش‌بینی رشد سه‌ماهه", "رشد ترافیک و فضای ذخیره‌سازی برای سه ماه آینده برآورد شود.", capacityPlanning, members.get(6), manager, TaskStatus.OPEN, Priority.HIGH, 0, now.plusDays(5), null),
                task("برآورد هزینه زیرساخت", "هزینه سناریوهای رشد پایه و خوش‌بینانه محاسبه شود.", capacityPlanning, members.get(7), manager, TaskStatus.PENDING, Priority.MEDIUM, 0, now.plusDays(6), null),
                task("دسته‌بندی درخواست‌های بحرانی", "پرونده‌های مشتریان بر اساس SLA و اثر کسب‌وکار مرتب شوند.", customerEscalations, members.get(0), manager, TaskStatus.IN_PROGRESS, Priority.CRITICAL, 55, now.plusDays(2), null),
                task("تهیه پاسخ وضعیت مشتری", "برای هر پرونده بحرانی یک پیام وضعیت قابل ارسال آماده شود.", customerEscalations, members.get(2), manager, TaskStatus.OPEN, Priority.HIGH, 0, now.plusDays(3), null),
                task("جمع‌آوری شاخص‌های محصول", "شاخص‌های انتشار و استفاده برای نشست ماهانه آماده شود.", monthlyAllHands, members.get(1), manager, TaskStatus.OPEN, Priority.MEDIUM, 0, now.plusDays(8), null),
                task("آماده‌سازی گزارش پایداری", "SLO، رخدادها و روند هشدارها در یک گزارش خلاصه شود.", monthlyAllHands, members.get(5), manager, TaskStatus.IN_PROGRESS, Priority.HIGH, 25, now.plusDays(8), null),
                task("خلاصه بازخورد مشتریان", "موضوعات پرتکرار و پیشنهادهای مشتریان دسته‌بندی شود.", monthlyAllHands, members.get(4), manager, TaskStatus.PENDING, Priority.MEDIUM, 0, now.plusDays(9), null),
                task("بررسی جایگزین سرویس ویدئو", "گزینه‌های جایگزین از نظر ظرفیت و هزینه مقایسه شوند.", cancelledVendorSync, members.get(4), manager, TaskStatus.CANCELLED, Priority.LOW, 0, now.plusDays(4), null),
                task("ثبت سلامت سرویس‌های امروز", "نتیجه بررسی داشبوردها در گزارش روزانه درج شود.", operationsStandup, members.get(6), manager, TaskStatus.IN_PROGRESS, Priority.MEDIUM, 70, now.plusHours(3), null),
                task("پیگیری هشدار پایگاه داده", "هشدار کندی کوئری بررسی و نتیجه در کانال عملیات اعلام شود.", operationsStandup, members.get(7), manager, TaskStatus.OPEN, Priority.HIGH, 20, now.plusHours(5), null)
        );
        taskRepository.saveAll(tasks);
    }

    private Team team(String name, String description) {
        Team team = new Team();
        team.setName(name);
        team.setDescription(description);
        return team;
    }

    private TeamMembership teamMembership(Team team, AppUser user, TeamRole role) {
        TeamMembership membership = new TeamMembership();
        membership.setTeam(team);
        membership.setUser(user);
        membership.setRole(role);
        return membership;
    }

    private Meeting meeting(Team team, AppUser organizer, String title, String description,
                            LocalDateTime startTime, int durationMinutes, String location,
                            MeetingStatus status, List<? extends AppUser> invitedUsers,
                            List<String> agendaTopics, String noteContent) {
        Meeting meeting = new Meeting();
        meeting.setTeam(team);
        meeting.setOrganizer(organizer);
        meeting.setTitle(title);
        meeting.setDescription(description);
        meeting.setStartTime(startTime);
        meeting.setEndTime(startTime.plusMinutes(durationMinutes));
        meeting.setLocation(location);
        meeting.setStatus(status);

        addParticipant(meeting, organizer, RsvpStatus.ACCEPTED, status == MeetingStatus.COMPLETED);
        for (AppUser invitedUser : invitedUsers) {
            if (!invitedUser.getId().equals(organizer.getId())) {
                RsvpStatus rsvp = status == MeetingStatus.CANCELLED ? RsvpStatus.DECLINED : RsvpStatus.ACCEPTED;
                addParticipant(meeting, invitedUser, rsvp, status == MeetingStatus.COMPLETED);
            }
        }

        for (int index = 0; index < agendaTopics.size(); index++) {
            AgendaItem agendaItem = new AgendaItem();
            agendaItem.setMeeting(meeting);
            agendaItem.setTopic(agendaTopics.get(index));
            agendaItem.setDescription("گفت‌وگو و ثبت نتیجه برای «" + agendaTopics.get(index) + "»");
            agendaItem.setDisplayOrder(index);
            agendaItem.setDurationMinutes(Math.max(10, durationMinutes / agendaTopics.size()));
            agendaItem.setPresenter(index == 0 ? organizer : invitedUsers.get((index - 1) % invitedUsers.size()));
            meeting.getAgendaItems().add(agendaItem);
        }

        if (noteContent != null) {
            MeetingNote note = new MeetingNote();
            note.setMeeting(meeting);
            note.setAuthor(organizer);
            note.setContent(noteContent);
            note.setType(status == MeetingStatus.COMPLETED ? NoteType.DECISION : NoteType.GENERAL);
            meeting.getNotes().add(note);
        }
        return meeting;
    }

    private void addParticipant(Meeting meeting, AppUser user, RsvpStatus rsvpStatus, boolean attended) {
        MeetingParticipant participant = new MeetingParticipant();
        participant.setMeeting(meeting);
        participant.setUser(user);
        participant.setRsvpStatus(rsvpStatus);
        participant.setAttended(attended);
        meeting.getParticipants().add(participant);
    }

    private Task task(String title, String description, Meeting meeting, TeamMember assignee,
                      AppUser createdBy, TaskStatus status, Priority priority, int progress,
                      LocalDateTime dueDate, LocalDateTime completedAt) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setMeeting(meeting);
        task.setAssignedMember(assignee);
        task.setCreatedBy(createdBy);
        task.setStatus(status);
        task.setPriority(priority);
        task.setProgress(progress);
        task.setDueDate(dueDate);
        task.setCompletedAt(completedAt);
        return task;
    }

    private List<TeamMember> sortedTeamMembers() {
        return teamMemberRepository.findAll().stream()
                .sorted(Comparator.comparing(TeamMember::getUsername, String.CASE_INSENSITIVE_ORDER))
                .toList();
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
        teamMember.setPassword(passwordEncoder.encode(DEFAULT_TEAM_MEMBER_PASSWORD));
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
