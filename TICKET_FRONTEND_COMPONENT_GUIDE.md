# Ticket Frontend Component Guide (React + JavaScript)

This guide defines the frontend component boundaries, role-based visibility, and ticket workflows for the React application.

All examples use JavaScript and JSX. The project already has a working HTTP client and authentication setup. Reuse the existing API modules, authenticated client, token handling, base URL, and interceptors. Do not create another HTTP client or configure request infrastructure inside components.

## Non-Negotiable Authorization Rules

The UI must enforce these rules by hiding unavailable navigation items, fields, and actions. The backend remains the final authority; hiding a control is not a replacement for server-side authorization.

### Basic Information Management

Only a user with the `TEAM_MANAGER` role may create, edit, or delete basic/master data:

- Customers
- Team members and other user accounts
- Team managers
- SLA contracts
- Categories and other administrative reference data
- Roles, permissions, and access scopes

Customers and team members must not see create, edit, or delete controls for this data. They should work only with their permitted ticket views and ticket actions.

| Resource | `CUSTOMER` | `TEAM_MEMBER` | `TEAM_MANAGER` |
| --- | --- | --- | --- |
| Customers/users | No menu or management page | No menu or management page | Create, view, edit, delete |
| Team members/managers | No menu or management page | No menu or management page | Create, view, edit, delete |
| SLA contracts | No menu or management page | No menu or management page | Create, view, edit, delete |
| Categories/reference data | No menu or management page | No menu or management page | Create, view, edit, delete |
| Roles and permissions | No menu or management page | No menu or management page | Manage when `ACCESS_ADMIN` is effective |

### Ticket Capabilities

| Capability | `CUSTOMER` | Assigned `TEAM_MEMBER` | `TEAM_MANAGER` |
| --- | --- | --- | --- |
| See ticket list | Own tickets only | Assigned tickets only | Tickets in management scope |
| Create ticket | Yes | No | Yes, when required by the management flow |
| See priority | No | Yes, read-only | Yes |
| See ticket/service scope | No | Yes, read-only | Yes |
| Edit title or description after creation | No | No | Yes |
| Change status | No | Yes, only when assigned | Yes |
| Add message | Yes, on own ticket | Yes, only when assigned | Yes |
| Upload attachment | Yes, on own ticket | Only if the product flow explicitly enables it | Yes |
| Change customer, SLA, assignee, priority, or scope | No | No | Yes |
| Delete ticket | Do not expose in the UI | Do not expose in the UI | Yes, within management scope |

An assigned expert may see priority and scope for triage context, but those fields must remain read-only. The expert's editing controls are limited to adding a message and changing ticket status. This avoids contradicting the rule that only managers change administrative or triage metadata.

## Authentication Session

Read the signed-in user and effective role from the existing authentication state. A practical session object has this shape:

```js
const session = {
  token: "jwt-token",
  userId: 42,
  roles: ["TEAM_MEMBER"],
  permissions: ["TICKET_READ", "TICKET_UPDATE"],
};
```

Centralize role and permission checks rather than scattering string comparisons across components:

```js
export const USER_ROLES = Object.freeze({
  CUSTOMER: "CUSTOMER",
  TEAM_MEMBER: "TEAM_MEMBER",
  TEAM_MANAGER: "TEAM_MANAGER",
});

export function hasRole(session, role) {
  return session?.roles?.includes(role) === true;
}

export function isManager(session) {
  return hasRole(session, USER_ROLES.TEAM_MANAGER);
}
```

## Role-Aware Sidebar

Build the sidebar from an allowlisted navigation configuration after authentication. Do not render an irrelevant link and wait for the destination page or API to return an empty result or `403`.

For example, a customer must not see `SLA Contracts`, `Customers`, `Users`, or `Access Management` in the sidebar. A team member must not see those links either. Only managers should receive administrative navigation entries.

```js
const navigationItems = [
  {
    key: "tickets",
    label: "Tickets",
    to: "/tickets",
    roles: ["CUSTOMER", "TEAM_MEMBER", "TEAM_MANAGER"],
  },
  {
    key: "new-ticket",
    label: "Create Ticket",
    to: "/tickets/new",
    roles: ["CUSTOMER", "TEAM_MANAGER"],
  },
  {
    key: "customers",
    label: "Customers",
    to: "/customers",
    roles: ["TEAM_MANAGER"],
  },
  {
    key: "team-members",
    label: "Team Members",
    to: "/team-members",
    roles: ["TEAM_MANAGER"],
  },
  {
    key: "sla-contracts",
    label: "SLA Contracts",
    to: "/sla-contracts",
    roles: ["TEAM_MANAGER"],
  },
  {
    key: "access",
    label: "Access Management",
    to: "/admin/access",
    roles: ["TEAM_MANAGER"],
    permission: "ACCESS_ADMIN",
  },
];

export function getVisibleNavigation(session) {
  return navigationItems.filter((item) => {
    const roleAllowed = item.roles.some((role) => session.roles.includes(role));
    const permissionAllowed = !item.permission
      || session.permissions.includes(item.permission);

    return roleAllowed && permissionAllowed;
  });
}
```

Also protect routes. A user who manually enters an unauthorized URL should be redirected to the first relevant page, normally `/tickets`, or shown a proper forbidden page.

```jsx
function ManagerRoute({ session, children }) {
  if (!session) {
    return <Navigate to="/login" replace />;
  }

  if (!isManager(session)) {
    return <Navigate to="/tickets" replace />;
  }

  return children;
}
```

## Ticket Endpoints Used by Components

Use the existing API service functions that wrap these backend endpoints.

| Use case | Method | Path | UI roles |
| --- | --- | --- | --- |
| Create ticket | `POST` | `/api/tickets` | `CUSTOMER`, `TEAM_MANAGER` |
| List team ticket summaries | `GET` | `/api/tickets` | `TEAM_MEMBER`, `TEAM_MANAGER` |
| Search role-scoped tickets | `POST` | `/api/tickets/search` | Authenticated roles |
| Get ticket detail | `GET` | `/api/tickets/{ticketId}` | Owner customer, assigned expert, manager |
| Update ticket/status | `PUT` | `/api/tickets/{ticketId}` | Assigned expert for status only; manager for full update |
| List messages | `GET` | `/api/tickets/{ticketId}/messages` | Authorized ticket participants |
| Add message | `POST` | `/api/tickets/{ticketId}/messages` | Authorized ticket participants |
| Add expert message | `POST` | `/api/team-members/tickets/{ticketId}/messages` | Assigned `TEAM_MEMBER` |
| Upload customer attachment | `POST` | `/api/customers/tickets/{ticketId}/attachments` | Owner `CUSTOMER` |
| Upload generic attachment | `POST` | `/api/tickets/{ticketId}/attachments` | Authorized roles when enabled by the UI flow |

## JavaScript Data Shapes

These contracts are shown as plain JavaScript objects.

### Ticket Creation

```js
const ticketCreateRequest = {
  title: "Cannot access dashboard",
  description: "The dashboard shows a blank page after login.",
  customerId: 15,
  slaContractId: 3,
  assignedMemberId: null,
};
```

For a customer-created ticket, derive `customerId` from the authenticated session/profile. Do not let a customer select another customer ID. Assignment is a management concern and should not appear in the customer form.

```js
const customerTicketCreateRequest = {
  title: form.title.trim(),
  description: form.description.trim(),
  customerId: session.userId,
  slaContractId: selectedSlaId || null,
  assignedMemberId: null,
};
```

### Ticket Statuses

```js
export const TICKET_STATUS = Object.freeze({
  UNALLOCATED: "UNALLOCATED",
  ASSIGNED: "ASSIGNED",
  IN_PROGRESS: "IN_PROGRESS",
  RESOLVED: "RESOLVED",
  CLOSED: "CLOSED",
});
```

The backend validates transitions. The frontend should offer only valid next statuses:

```js
export const nextTicketStatuses = Object.freeze({
  UNALLOCATED: ["ASSIGNED", "CLOSED"],
  ASSIGNED: ["IN_PROGRESS", "UNALLOCATED", "CLOSED"],
  IN_PROGRESS: ["RESOLVED", "CLOSED", "ASSIGNED"],
  RESOLVED: ["CLOSED", "IN_PROGRESS"],
  CLOSED: [],
});
```

An assigned expert sends only the status fields from the edit form:

```js
const expertStatusUpdate = {
  status: selectedStatus,
  statusNote: note.trim() || null,
};
```

Do not include `title`, `description`, `slaContractId`, `assignedMemberId`, `priority`, or scope fields in an expert update request.

### Ticket Message

```js
const ticketMessageRequest = {
  message: message.trim(),
};
```

### Search Request

```js
const ticketSearchRequest = {
  title: filters.title || undefined,
  status: filters.status || undefined,
  createdFrom: filters.createdFrom || undefined,
  createdTo: filters.createdTo || undefined,
};
```

Do not add customer-only `priority` or scope filters. The server derives `customerId`, `assignedToId`, `teamId`, `userType`, and `userId` from the authenticated actor; components should not use those fields to widen access.

## Recommended JavaScript Component Structure

Use `.js` or `.jsx` files consistently with the existing project convention.

```text
src/features/tickets/
  api/ticketsApi.js
  components/TicketList.jsx
  components/TicketSearchBar.jsx
  components/TicketDetail.jsx
  components/TicketCreateForm.jsx
  components/TicketMessageThread.jsx
  components/TicketMessageComposer.jsx
  components/CustomerAttachmentUploader.jsx
  components/TicketStatusEditor.jsx
  components/ManagerTicketEditor.jsx
  hooks/useTicket.js
  hooks/useTicketSearch.js
```

## Ticket List

The list must be role-scoped:

- A customer sees only tickets owned by that customer.
- A team member sees only tickets assigned to that member.
- A manager sees tickets within the manager's effective scope.
- Customer rows must omit priority, emergency, and scope columns entirely.
- Team-member rows may show priority and scope, but they are display-only.
- Management actions must not appear for customers or team members.

Avoid disabled or empty administrative actions when they are irrelevant. Do not show an edit icon that always produces `403`.

```jsx
function TicketListColumns({ role }) {
  const columns = ["title", "status", "createdAt"];

  if (role === "TEAM_MEMBER" || role === "TEAM_MANAGER") {
    columns.push("priority", "scope");
  }

  if (role === "TEAM_MANAGER") {
    columns.push("customer", "assignee", "actions");
  }

  return <TicketTable columns={columns} />;
}
```

## Ticket Detail

`TicketDetail` owns presentation; role-specific child components own actions.

| Role | Visible information | Visible actions |
| --- | --- | --- |
| `CUSTOMER` | Title, description, status, dates, messages, attachments | Add message, upload attachment |
| Assigned `TEAM_MEMBER` | Customer-visible fields plus priority and scope | Add message, change status |
| `TEAM_MANAGER` | All ticket fields, priority, scope, SLA, customer, assignment, history | Full allowed management actions |

Customer components must not render priority or scope in the DOM. Do not merely hide them with CSS because hidden DOM content is still exposed to browser tools and accessibility APIs. Prefer a customer-specific response projection from the backend and pass only allowed fields to customer components.

```jsx
function TicketDetailActions({ session, ticket }) {
  const role = session.roles[0];
  const isAssignedExpert = role === "TEAM_MEMBER"
    && ticket.assignedMemberId === session.userId;

  return (
    <>
      {role === "CUSTOMER" && (
        <>
          <TicketMessageComposer ticketId={ticket.id} />
          <CustomerAttachmentUploader ticketId={ticket.id} />
        </>
      )}

      {isAssignedExpert && (
        <>
          <TicketMessageComposer ticketId={ticket.id} />
          <TicketStatusEditor ticket={ticket} />
        </>
      )}

      {role === "TEAM_MANAGER" && <ManagerTicketEditor ticket={ticket} />}
    </>
  );
}
```

## Customer Ticket Creation and File Uploads

Customers may attach files while creating a ticket and while viewing/updating an existing ticket.

The current ticket creation endpoint accepts JSON, while file upload requires multipart data and an existing ticket ID. Implement “upload during creation” as a two-step UI flow:

1. Validate the ticket form and selected files.
2. Create the ticket with `POST /api/tickets`.
3. Read the new ticket ID from the response.
4. Upload each selected file to `POST /api/customers/tickets/{ticketId}/attachments`.
5. Navigate to the ticket detail after uploads finish.
6. If an upload fails, keep the successfully created ticket and show which files need retrying.

```jsx
function TicketCreateForm({ session, createTicket, uploadAttachment }) {
  const [files, setFiles] = useState([]);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setIsSubmitting(true);

    try {
      const ticket = await createTicket({
        title: event.currentTarget.title.value.trim(),
        description: event.currentTarget.description.value.trim(),
        customerId: session.userId,
        slaContractId: null,
        assignedMemberId: null,
      });

      const uploads = files.map((file) => uploadAttachment(ticket.id, file));
      await Promise.allSettled(uploads);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <input name="title" required />
      <textarea name="description" required />
      <input
        type="file"
        multiple
        onChange={(event) => setFiles(Array.from(event.target.files || []))}
      />
      <button disabled={isSubmitting} type="submit">
        {isSubmitting ? "Creating..." : "Create ticket"}
      </button>
    </form>
  );
}
```

For an existing ticket, render the same uploader in the customer detail page. The API service should create `FormData`; components should call the existing upload function rather than configuring multipart headers themselves.

## Assigned Expert Editing

An expert is allowed to act only when the ticket is assigned to that expert.

The expert may:

- Read priority and scope.
- Add a message.
- Change the ticket status using a valid transition.
- Add a meaningful status note.

The expert may not:

- Change title or description.
- Change customer or SLA contract.
- Change assignee from the general edit form.
- Change priority or scope.
- Edit customer, user, SLA, role, or category records.

Render priority and scope as text or a read-only value, never as an input for `TEAM_MEMBER`:

```jsx
function ExpertTriageInfo({ ticket }) {
  return (
    <dl>
      <dt>Priority</dt>
      <dd>{ticket.priority}</dd>
      <dt>Scope</dt>
      <dd>{ticket.scope}</dd>
    </dl>
  );
}
```

## Manager Forms

Manager-only pages may expose create, edit, and delete operations for customers, users, team members, SLA contracts, categories, and access control.

Every manager-only form should:

- Check `TEAM_MANAGER` before rendering.
- Check fine-grained permissions when the endpoint requires them.
- Hide destructive controls from all other roles.
- Confirm delete operations and explain the consequence.
- Invalidate/refetch affected list and detail queries after mutation.
- Display API validation errors next to the relevant field.

Do not reuse a manager form for a customer or expert by disabling half of its fields. Use smaller role-specific components with only the permitted data.

## State and Query Management

Use the existing application state and API service. Local state plus custom hooks is enough for form-only state. If the project already uses a server-state library, keep its established query keys.

```js
function useTicket(ticketId) {
  // Use the existing ticket API module.
}

function useTicketSearch(filters) {
  // Use the existing role-scoped search API module.
}
```

After mutations, refresh only relevant data:

| Operation | Data to refresh |
| --- | --- |
| Create ticket | Ticket list and new ticket detail |
| Add message | Ticket detail/messages |
| Upload attachment | Ticket detail/attachments |
| Change status | Ticket detail, ticket list, status history |
| Manager updates basic information | Corresponding list and detail |

## Error Handling

| Status | Meaning | UI behavior |
| --- | --- | --- |
| `400` | Invalid data or status transition | Show a field/form error and preserve input |
| `401` | Missing or expired authentication | Return to sign-in through the existing auth flow |
| `403` | Role, ownership, assignment, or permission denied | Show a concise forbidden message; fix any UI control that should have been hidden |
| `404` | Ticket or related record not found | Show a not-found state |
| `500` | Unexpected server/storage failure | Show retry guidance without losing form data |

A customer upload may return `403` when the ticket belongs to another customer. An expert action may return `403` when the ticket is not assigned to that expert.

## Manual Test Checklist

### Navigation

- Customer sidebar contains ticket list and ticket creation, but no users, customers, SLA, categories, or access links.
- Team-member sidebar contains the assigned ticket list, but no basic-information management links.
- Manager sidebar contains authorized management links.
- Direct navigation to a manager route is rejected for customers and team members.
- No role sees a link that opens a permanently empty or forbidden page.

### Customer

- Customer sees only owned tickets.
- Customer can create a ticket.
- Customer can select files during ticket creation and failed uploads can be retried.
- Customer can upload files from an existing owned ticket.
- Customer can add messages to an owned ticket.
- Customer never sees priority or scope in list, detail, filters, exports, or hidden DOM content.
- Customer cannot manage users, customers, team members, SLA contracts, or categories.

### Assigned Expert

- Team member sees only assigned tickets.
- Assigned expert sees priority and scope as read-only values.
- Assigned expert can add a message and change status using valid transitions.
- Unassigned expert cannot edit the ticket.
- Expert cannot edit title, description, SLA, customer, assignee, priority, or scope.
- Expert cannot manage basic/master data.

### Manager

- Manager can access customer, user, team-member, SLA, category, and access-management pages when authorized.
- Manager can create, edit, and delete basic information.
- Manager sees all relevant ticket fields and management controls.
- Delete actions require confirmation and refresh affected data.

### General

- Pending actions disable duplicate submission.
- Empty messages and empty file uploads are blocked.
- Successful messages, status changes, and uploads refresh the ticket detail.
- `401`, `403`, validation, not-found, and retry states are clear and role-appropriate.
