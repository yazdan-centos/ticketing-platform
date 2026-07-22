# Task management module — drop-in files for `ticketing-platform`

Heads-up: the repo link you gave me (`yazdan-centos/collaboration2`) is your
frontend-only React demo dashboard — no Java in it. Your actual Spring Boot
backend is `yazdan-centos/ticketing-platform` (`com.mapnaom.ticketingplatform`),
which has no `task` package yet, so all of this is new. I matched its existing
conventions exactly (compare against `Ticket`/`TicketListMapper`/`TicketSpecification`/
`TicketController`).

## Where each file goes
Copy the `com/` folder straight into `src/main/java/` of the backend project.
Package paths already match:

- `model/Task.java`
- `model/enums/TaskStatus.java`
- `dto/task/TaskDto.java`
- `dto/task/TaskSearchRequestDto.java`
- `mapper/TaskMapper.java` (MapStruct — generates `TaskMapperImpl` on build)
- `repository/TaskRepository.java`
- `specification/TaskSpecification.java`
- `service/TaskService.java`
- `service/impl/TaskServiceImpl.java`
- `controller/TaskController.java`

## Reused, not duplicated
- **`Priority` enum** — reused as-is from `model/enums/Priority.java`
  (LOW/MEDIUM/HIGH/CRITICAL). No new priority enum was created.
- **`TeamMember`** — task assignment reuses the existing `TeamMember`
  entity/repository, same as `Ticket.assignedMember`.

## New
- **`TaskStatus`** enum (PENDING / IN_PROGRESS / COMPLETED / FAILED) — matches
  the four statuses your `TaskTable.jsx` / `dashboardData.js` frontend already
  renders (`pending`, `in-progress`, `completed`, `failed`).

## Endpoints (mirrors `TicketController`'s shape)
- `POST   /api/tasks` — create
- `GET    /api/tasks` — list all
- `GET    /api/tasks/{id}` — get one
- `PUT    /api/tasks/{id}` — update
- `DELETE /api/tasks/{id}` — delete
- `POST   /api/tasks/search?page=&size=&sortBy=&order=` — dynamic filtering
  (title contains, status, priority, assignee, due-date range, created-date
  range), paginated and sorted, backed by `TaskSpecification`.

## Flyway migration
`db/migration/V1__create_tasks_table.sql` creates the `tasks` table, matching
the column types/style already in `static/ddl.sql` and reusing
`app_users(id)` as the FK target for `assigned_member_id` (same as
`tickets.assigned_member_id`, since `TeamMember` is single-table inheritance
over `AppUser`). Drop it into `src/main/resources/db/migration/`.

Rename the `V1__` prefix if you already have other migrations not in this
checkout — Flyway needs a version higher than whatever you're currently at.

**One thing worth checking:** your `application.properties` currently has
`spring.jpa.hibernate.ddl-auto=create` active and the `spring.flyway.*` lines
commented out, even though a comment there says "Schema is owned by Flyway."
As it stands, Hibernate drops and rebuilds the whole schema on every
startup, so this migration won't do anything until you flip
`ddl-auto` to `validate` (or `none`) and uncomment/enable the Flyway lines.

No frontend wiring included — server-side Java + this one SQL file only, per
your original scope constraints.
