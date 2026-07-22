# Team Meeting Management — Backend Design

Spring Boot + JPA backend for scheduling, running, and tracking team meetings across multiple teams and users. Follows the same layered structure and conventions used on your ticketing platform: DTOs at the boundary, soft-delete on core entities, paginated list endpoints, an `ApiResponse<T>` wrapper, and a `GlobalExceptionHandler`.

## 1. Domain Model

| Entity | Purpose |
|---|---|
| `Team` | A group that owns meetings and has members |
| `User` | A person; belongs to one or more teams |
| `TeamMembership` | Join entity: user ↔ team, with a role (MEMBER / LEAD) |
| `Meeting` | A scheduled meeting owned by a team |
| `AgendaItem` | An ordered topic within a meeting |
| `MeetingParticipant` | Join entity: meeting ↔ invited user, with RSVP + attendance |
| `MeetingNote` | A note/action-item/decision recorded against a meeting |
| `Task` | An action item or task assigned from a meeting |

### Task Fields

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | Long | PK, auto-generated | Unique identifier |
| `title` | String | NOT NULL | Task title/summary |
| `description` | String | TEXT | Detailed task description |
| `meeting` | Meeting | FK, NOT NULL | Reference to parent meeting |
| `assignee` | User | FK, NOT NULL | User assigned to the task |
| `status` | TaskStatus (enum) | NOT NULL | OPEN, IN_PROGRESS, COMPLETED, CANCELLED |
| `priority` | TaskPriority (enum) | NOT NULL | LOW, MEDIUM, HIGH |
| `dueDate` | LocalDateTime | NOT NULL | When the task is due |
| `completedAt` | LocalDateTime | Nullable | When the task was completed |
| `createdBy` | User | FK, NOT NULL | User who created the task |
| `createdAt` | LocalDateTime | NOT NULL, @CreationTimestamp | Timestamp when created |
| `updatedAt` | LocalDateTime | @UpdateTimestamp | Timestamp when last updated |
| `active` | Boolean | NOT NULL, default=true | Soft delete flag |

Relationships: `Team 1—N Meeting`, `Team 1—N TeamMembership N—1 User`, `Meeting 1—N AgendaItem`, `Meeting 1—N MeetingParticipant N—1 User`, `Meeting 1—N MeetingNote`.

---

## 2. Entities

```java
@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamMembership> memberships = new ArrayList<>();

    @OneToMany(mappedBy = "team")
    private List<Meeting> meetings = new ArrayList<>();

    @Column(nullable = false)
    private boolean active = true; // soft delete

    @CreationTimestamp
    private LocalDateTime createdAt;

    // getters/setters
}
```

```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String fullName;

    @OneToMany(mappedBy = "user")
    private List<TeamMembership> teamMemberships = new ArrayList<>();

    @Column(nullable = false)
    private boolean active = true;

    // getters/setters
}
```

```java
@Entity
@Table(name = "team_memberships",
       uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "user_id"}))
public class TeamMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamRole role; // MEMBER, LEAD

    @CreationTimestamp
    private LocalDateTime joinedAt;

    // getters/setters
}
```

```java
@Entity
@Table(name = "meetings")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    private String location; // room name or video link

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingStatus status; // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<AgendaItem> agendaItems = new ArrayList<>();

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetingParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetingNote> notes = new ArrayList<>();

    @Column(nullable = false)
    private boolean active = true; // soft delete

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // getters/setters
}
```

```java
@Entity
@Table(name = "agenda_items")
public class AgendaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Column(nullable = false)
    private String topic;

    private String description;

    @Column(nullable = false)
    private Integer displayOrder;

    private Integer durationMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presenter_id")
    private User presenter;

    // getters/setters
}
```

```java
@Entity
@Table(name = "meeting_participants",
       uniqueConstraints = @UniqueConstraint(columnNames = {"meeting_id", "user_id"}))
public class MeetingParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RsvpStatus rsvpStatus; // PENDING, ACCEPTED, DECLINED, TENTATIVE

    private boolean attended;

    // getters/setters
}
```

```java
@Entity
@Table(name = "meeting_notes")
public class MeetingNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NoteType type; // GENERAL, ACTION_ITEM, DECISION

    @CreationTimestamp
    private LocalDateTime createdAt;

    // getters/setters
}
```

Enums (`TeamRole`, `MeetingStatus`, `RsvpStatus`, `NoteType`) are plain `enum` types stored as `STRING` for readability in the DB and safety across future reorderings.

---

## 3. DTOs (boundary layer)

Keep entities off the wire. Two representative examples — the rest of the DTOs follow the same request/response split:

```java
public record MeetingCreateRequest(
        @NotBlank String title,
        String description,
        @NotNull Long teamId,
        @NotNull Long organizerId,
        @NotNull @Future LocalDateTime startTime,
        @NotNull @Future LocalDateTime endTime,
        String location,
        List<Long> participantUserIds) {}

public record MeetingResponse(
        Long id,
        String title,
        String description,
        Long teamId,
        String teamName,
        Long organizerId,
        String organizerName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String location,
        MeetingStatus status,
        List<AgendaItemResponse> agendaItems,
        int participantCount) {}
```

`MeetingMapper` (a `@Component`, or MapStruct if you'd rather generate it) converts between `Meeting` and these records.

---

## 4. Repositories

```java
public interface TeamRepository extends JpaRepository<Team, Long> {
    Page<Team> findByActiveTrue(Pageable pageable);
}

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

public interface TeamMembershipRepository extends JpaRepository<TeamMembership, Long> {
    List<TeamMembership> findByTeamId(Long teamId);
    List<TeamMembership> findByUserId(Long userId);
    Optional<TeamMembership> findByTeamIdAndUserId(Long teamId, Long userId);
}

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    Page<Meeting> findByTeamIdAndActiveTrue(Long teamId, Pageable pageable);

    List<Meeting> findByOrganizerIdAndActiveTrue(Long organizerId);

    @Query("""
           SELECT DISTINCT m FROM Meeting m JOIN m.participants p
           WHERE p.user.id = :userId AND m.active = true
           AND m.startTime BETWEEN :from AND :to
           ORDER BY m.startTime ASC
           """)
    List<Meeting> findUpcomingForUser(@Param("userId") Long userId,
                                       @Param("from") LocalDateTime from,
                                       @Param("to") LocalDateTime to);

    @Query("""
           SELECT COUNT(m) > 0 FROM Meeting m
           WHERE m.team.id = :teamId AND m.active = true AND m.id <> :excludeId
           AND m.startTime < :endTime AND m.endTime > :startTime
           """)
    boolean existsOverlappingMeeting(@Param("teamId") Long teamId,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime,
                                      @Param("excludeId") Long excludeId);
}

public interface AgendaItemRepository extends JpaRepository<AgendaItem, Long> {
    List<AgendaItem> findByMeetingIdOrderByDisplayOrderAsc(Long meetingId);
}

public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {
    List<MeetingParticipant> findByMeetingId(Long meetingId);
    Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId);
}

public interface MeetingNoteRepository extends JpaRepository<MeetingNote, Long> {
    List<MeetingNote> findByMeetingIdOrderByCreatedAtAsc(Long meetingId);
}
```

---

## 5. Service Layer

Interfaces define the contract; `@Service` implementations hold the business rules (conflict checks, RSVP transitions, permission checks delegated to a `@PreAuthorize`-guarded controller layer).

```java
public interface MeetingService {

    MeetingResponse createMeeting(MeetingCreateRequest request);

    MeetingResponse getMeeting(Long meetingId);

    Page<MeetingResponse> getMeetingsForTeam(Long teamId, Pageable pageable);

    List<MeetingResponse> getUpcomingMeetingsForUser(Long userId, LocalDateTime from, LocalDateTime to);

    MeetingResponse updateMeeting(Long meetingId, MeetingUpdateRequest request);

    void cancelMeeting(Long meetingId);       // sets status = CANCELLED
    void deleteMeeting(Long meetingId);       // soft delete: active = false

    void addParticipants(Long meetingId, List<Long> userIds);
    void respondToInvite(Long meetingId, Long userId, RsvpStatus response);
    void markAttendance(Long meetingId, Long userId, boolean attended);

    AgendaItemResponse addAgendaItem(Long meetingId, AgendaItemRequest request);
    void reorderAgenda(Long meetingId, List<Long> orderedAgendaItemIds);

    MeetingNoteResponse addNote(Long meetingId, MeetingNoteRequest request);
    List<MeetingNoteResponse> getNotes(Long meetingId);
}
```

Key responsibilities in `MeetingServiceImpl`:

- **`createMeeting`** — validates the organizer belongs to the team, checks `existsOverlappingMeeting` for scheduling conflicts, persists the meeting plus initial `MeetingParticipant` rows (status `PENDING`) for the invited user IDs.
- **`respondToInvite`** — loads the `MeetingParticipant` row and transitions `rsvpStatus`; rejects the call if the meeting is `CANCELLED`.
- **`cancelMeeting` / `deleteMeeting`** — cancel is a status change (meeting stays visible in history); delete is the soft-delete flag, consistent with how the ticketing platform handles removal.
- **`reorderAgenda`** — takes an ordered list of `AgendaItem` IDs and rewrites `displayOrder` in one transaction.

```java
public interface TeamService {
    TeamResponse createTeam(TeamCreateRequest request);
    TeamResponse getTeam(Long teamId);
    Page<TeamResponse> getTeams(Pageable pageable);
    void addMember(Long teamId, Long userId, TeamRole role);
    void removeMember(Long teamId, Long userId);
    void deleteTeam(Long teamId);
}
```

---

## 6. Controllers

All list endpoints are paginated; all responses go through `ApiResponse<T>`.

```java
@RestController
@RequestMapping("/api/v1/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping
    @PreAuthorize("hasRole('TEAM_LEAD') or hasRole('TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<MeetingResponse>> createMeeting(
            @Valid @RequestBody MeetingCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(meetingService.createMeeting(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MeetingResponse>> getMeeting(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(meetingService.getMeeting(id)));
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<ApiResponse<Page<MeetingResponse>>> getTeamMeetings(
            @PathVariable Long teamId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(meetingService.getMeetingsForTeam(teamId, pageable)));
    }

    @GetMapping("/user/{userId}/upcoming")
    public ResponseEntity<ApiResponse<List<MeetingResponse>>> getUpcomingForUser(
            @PathVariable Long userId,
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to) {
        return ResponseEntity.ok(ApiResponse.success(
                meetingService.getUpcomingMeetingsForUser(userId, from, to)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEAM_LEAD')")
    public ResponseEntity<ApiResponse<MeetingResponse>> updateMeeting(
            @PathVariable Long id, @Valid @RequestBody MeetingUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(meetingService.updateMeeting(id, request)));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('TEAM_LEAD')")
    public ResponseEntity<ApiResponse<Void>> cancelMeeting(@PathVariable Long id) {
        meetingService.cancelMeeting(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEAM_LEAD')")
    public ResponseEntity<ApiResponse<Void>> deleteMeeting(@PathVariable Long id) {
        meetingService.deleteMeeting(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/participants")
    public ResponseEntity<ApiResponse<Void>> addParticipants(
            @PathVariable Long id, @RequestBody List<Long> userIds) {
        meetingService.addParticipants(id, userIds);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/participants/{userId}/rsvp")
    public ResponseEntity<ApiResponse<Void>> respondToInvite(
            @PathVariable Long id, @PathVariable Long userId, @RequestParam RsvpStatus response) {
        meetingService.respondToInvite(id, userId, response);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/agenda")
    public ResponseEntity<ApiResponse<AgendaItemResponse>> addAgendaItem(
            @PathVariable Long id, @Valid @RequestBody AgendaItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(meetingService.addAgendaItem(id, request)));
    }

    @PutMapping("/{id}/agenda/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderAgenda(
            @PathVariable Long id, @RequestBody List<Long> orderedIds) {
        meetingService.reorderAgenda(id, orderedIds);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<ApiResponse<MeetingNoteResponse>> addNote(
            @PathVariable Long id, @Valid @RequestBody MeetingNoteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(meetingService.addNote(id, request)));
    }

    @GetMapping("/{id}/notes")
    public ResponseEntity<ApiResponse<List<MeetingNoteResponse>>> getNotes(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(meetingService.getNotes(id)));
    }
}
```

```java
@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(@Valid @RequestBody TeamCreateRequest request) { ... }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TeamResponse>>> getTeams(Pageable pageable) { ... }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamResponse>> getTeam(@PathVariable Long id) { ... }

    @PostMapping("/{id}/members")
    public ResponseEntity<ApiResponse<Void>> addMember(
            @PathVariable Long id, @RequestParam Long userId, @RequestParam TeamRole role) { ... }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long id, @PathVariable Long userId) { ... }
}
```

---

## 7. Cross-cutting

**`ApiResponse<T>`** — same envelope pattern as the ticketing platform: `{ success, data, message, timestamp }`, built via static factories `ApiResponse.success(data)` / `ApiResponse.error(message)`.

**`GlobalExceptionHandler`** — `@RestControllerAdvice` mapping:
- `MeetingConflictException` (overlapping meeting) → 409
- `EntityNotFoundException` → 404
- `MethodArgumentNotValidException` (bean validation) → 400 with field errors
- `AccessDeniedException` → 403
- fallback → 500 with a public nested `ErrorResponse` record

**Security** — reuse the existing JWT + role model: `TEAM_MEMBER` can create meetings and RSVP; `TEAM_LEAD` can update/cancel/delete and manage membership, enforced with `@PreAuthorize` SpEL on top of URL-level guards.

**Soft delete** — `Meeting` and `Team` carry an `active` flag rather than a hard delete, so meeting history stays queryable for reporting even after a team or meeting is "removed."

**Pagination** — every list endpoint takes a Spring `Pageable` and returns `Page<T>`, matching the dashboard/CRUD endpoints already in place on the ticketing platform.

---

## 8. Suggested package layout

```
com.yourorg.meetingmanagement
├── entity/        Team, User, TeamMembership, Meeting, AgendaItem, MeetingParticipant, MeetingNote
├── dto/            request/ and response/ records
├── repository/
├── service/        interfaces
│   └── impl/
├── controller/
├── mapper/
├── exception/       MeetingConflictException, GlobalExceptionHandler, ErrorResponse
├── security/         JWT filter, RBAC config
└── config/
```
