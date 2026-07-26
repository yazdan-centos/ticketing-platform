# ReactJS File Upload Implementation Agenda

This guide is the implementation contract for ReactJS developers adding file uploads to the ticketing platform. It covers ticket attachments and profile avatars exposed by the current Spring Boot API.

> Status: the backend supports upload, download, and attachment deletion.

## 1. Agenda

1. Identify the upload flow: ticket attachment or avatar.
2. Select the endpoint from the signed-in user's role and target resource.
3. Add the API functions to the existing authenticated HTTP service.
4. Build an accessible file picker, selected-file list, and validation messages.
5. Send the file in a `FormData` field named exactly `file`.
6. Track idle, uploading, success, partial-success, and failure states per file.
7. Update the screen from the returned DTO or refetch the target resource.
8. Add delete support where the product allows it.
9. Verify authorization, payload-size, network-failure, retry, and mobile states.

## 2. API Contract

### Endpoint matrix

| Purpose | Method and path | Role/API rule | Success |
| --- | --- | --- | --- |
| Staff ticket attachment | `POST /api/tickets/{ticketId}/attachments` | `TEAM_MEMBER` or `TEAM_MANAGER`; JWT required | `201` + attachment DTO |
| Customer ticket attachment | `POST /api/customers/tickets/{ticketId}/attachments` | `CUSTOMER`; the ticket must belong to that customer | `201` + attachment DTO |
| Download ticket attachment | `GET /api/tickets/attachments/{attachmentId}` | Authenticated request | File body with download headers |
| Delete ticket attachment | `DELETE /api/tickets/attachments/{attachmentId}` | Authenticated `CUSTOMER`, `TEAM_MEMBER`, or `TEAM_MANAGER` | `204` |
| Customer avatar | `POST /api/customers/{id}/avatar` | Own profile or `TEAM_MANAGER` | `200` + customer DTO |
| Team member avatar | `POST /api/team-members/{id}/avatar` | `TEAM_MEMBER` or `TEAM_MANAGER` | `200` + team-member DTO |
| Team manager avatar | `POST /api/team-managers/{id}/avatar` | `TEAM_MANAGER` | `200` + team-manager DTO |
| Delete team member avatar | `DELETE /api/team-members/{id}/avatar` | `TEAM_MEMBER` or `TEAM_MANAGER` | `204` |

All upload endpoints consume `multipart/form-data` and expect one part named `file`.

### Staff ticket upload

```text
POST /api/tickets/{ticketId}/attachments
Authorization: Bearer <jwt>
Content-Type: multipart/form-data
Part name: file
```

Use this route for team-member and team-manager screens. Although the security configuration currently also admits the `CUSTOMER` role to this route, customer UI must use the ownership-checking customer endpoint below.

### Customer ticket upload

```text
POST /api/customers/tickets/{ticketId}/attachments
Authorization: Bearer <jwt>
Content-Type: multipart/form-data
Part name: file
```

Use this route for a customer-owned ticket. The backend rejects uploads when the authenticated customer does not own the ticket.

### Remove an attachment

```text
DELETE /api/tickets/attachments/{attachmentId}
Authorization: Bearer <jwt>
```

The response is `204 No Content`.

### Upload response

The upload response contains attachment metadata:

```json
{
  "id": 42,
  "ticketId": 17,
  "fileName": "evidence.png",
  "contentType": "image/png",
  "size": 24567,
  "filePath": "...",
  "uploadedById": 9,
  "uploadedAt": "2026-07-25T10:30:00"
}
```

Treat `filePath` as internal metadata. Never render it, log it to analytics, or turn it into a browser URL. Download with `GET /api/tickets/attachments/{attachmentId}` using the attachment `id`. `/uploads/**` is the public resource mapping used by avatar URLs returned in profile DTOs.

### Current validation rules

- Ticket attachments must be non-empty and have a file name. The backend currently accepts any attachment MIME type.
- Avatars must be non-empty images whose `Content-Type` starts with `image/`.
- No multipart size limit is explicitly configured in `application.properties`. React code must not invent a limit that can drift from the server. Add a shared documented limit to the backend before enforcing the same value in the UI.
- The frontend checks improve feedback only. The backend remains the security and validation boundary.

## 3. Recommended React Component

```jsx
import { useState } from "react";

export default function AttachmentUploader({ ticketId, uploadAttachment, maxFileSize }) {
  const [files, setFiles] = useState([]);
  const [status, setStatus] = useState("idle");
  const [error, setError] = useState("");

  function selectFiles(event) {
    const selected = Array.from(event.target.files || []);
    const invalid = selected.find((file) => (
      file.size === 0 || (maxFileSize && file.size > maxFileSize)
    ));

    if (invalid) {
      setError(`File is empty or too large: ${invalid.name}`);
      setFiles([]);
      return;
    }

    setError("");
    setFiles(selected);
  }

  async function submit(event) {
    event.preventDefault();
    if (!files.length || status === "uploading") return;

    setStatus("uploading");
    setError("");
    const results = await Promise.allSettled(
      files.map((file) => uploadAttachment(ticketId, file))
    );

    const failed = results.filter((result) => result.status === "rejected");
    if (failed.length) {
      setStatus("partial");
      setError(`${failed.length} file(s) failed. Keep the selection available for retry.`);
    } else {
      setStatus("success");
      setFiles([]);
    }
  }

  return (
    <form onSubmit={submit}>
      <label htmlFor="attachment">Attach files</label>
      <input
        id="attachment"
        type="file"
        multiple
        onChange={selectFiles}
        disabled={status === "uploading"}
      />
      {files.map((file) => <div key={`${file.name}-${file.lastModified}`}>{file.name}</div>)}
      {error && <p role="alert">{error}</p>}
      {status === "success" && <p role="status">Files uploaded successfully.</p>}
      <button type="submit" disabled={!files.length || status === "uploading"}>
        {status === "uploading" ? "Uploading..." : "Upload"}
      </button>
    </form>
  );
}
```

## 4. API Service Helper

Keep request construction in a service/helper, not inside the presentational component. Do not manually set the `Content-Type` header; the browser must add the multipart boundary.

```js
export async function uploadTicketAttachment(ticketId, file, token) {
  const body = new FormData();
  body.append("file", file);

  const response = await fetch(`/api/tickets/${ticketId}/attachments`, {
    method: "POST",
    headers: { Authorization: `Bearer ${token}` },
    body,
  });

  if (!response.ok) {
    const message = await response.text();
    const error = new Error(message || "Upload failed");
    error.status = response.status;
    throw error;
  }

  return response.json();
}
```

Use the customer URL for customer sessions. Keep this choice in the service layer or a role-aware container, not in the file-picker component.

```js
export function uploadCustomerTicketAttachment(ticketId, file, token) {
  return uploadFile(`/api/customers/tickets/${ticketId}/attachments`, file, token);
}

export function uploadAvatar(resource, id, file, token) {
  const allowedResources = new Set(["customers", "team-members", "team-managers"]);
  if (!allowedResources.has(resource)) throw new Error("Unsupported avatar resource");
  return uploadFile(`/api/${resource}/${id}/avatar`, file, token);
}

async function uploadFile(url, file, token) {
  const body = new FormData();
  body.append("file", file);

  const response = await fetch(url, {
    method: "POST",
    headers: { Authorization: `Bearer ${token}` },
    body,
  });

  if (!response.ok) {
    const payload = await response.json().catch(() => null);
    const error = new Error(payload?.message || "Upload failed");
    error.status = response.status;
    error.payload = payload;
    throw error;
  }

  return response.json();
}
```

If the React application already has an Axios/fetch wrapper and JWT interceptor, add these functions to that service instead of creating another HTTP client. With Axios, also let the browser generate the multipart boundary.

## 5. Ticket Creation Flow

Ticket creation currently accepts JSON while attachments require an existing ticket ID. Implement creation with two requests:

1. Validate the ticket form and selected files.
2. `POST /api/tickets` with the JSON ticket data.
3. Read the new ticket ID.
4. Upload selected files to the appropriate attachment endpoint.
5. Navigate to the ticket detail and refresh its attachments.
6. If only some uploads fail, preserve the ticket and show retry actions for failed files.

## 6. Avatar UI Rules

- Use `accept="image/*"` and validate `file.type.startsWith("image/")` before upload.
- Show a local preview with `URL.createObjectURL(file)` while the request is pending.
- Revoke replaced or unmounted preview URLs with `URL.revokeObjectURL(url)`.
- After success, replace the avatar with the URL in the returned DTO and add the API base URL when the DTO contains a relative `/uploads/...` path.
- Keep the previous avatar visible if upload fails.

## 7. UX and Error Rules

| Response | Frontend behavior |
| --- | --- |
| `201 Created` | Add returned metadata to the attachment list and clear the completed file. |
| `400 Bad Request` | Read the JSON `message`, show validation guidance, and keep the selected file. |
| `401 Unauthorized` | Refresh authentication or redirect to sign-in. |
| `403 Forbidden` | Explain that the current role or ticket access does not allow upload. |
| `404 Not Found` | Refresh the ticket or show that it no longer exists. |
| `413 Payload Too Large` | Tell the user to choose a smaller file. |
| `5xx` | Show a retry action without losing the form state. |

Disable duplicate submissions while an upload is active. Show file names, sizes, and a clear per-file status. Use object URLs for previews only and revoke them with `URL.revokeObjectURL` when they are no longer needed.

The global error response is JSON with `timestamp`, `status`, `error`, `message`, and `path`. Some controller-specific failures may have an empty response body, so always provide a local fallback message.

## 8. Security Checklist

- Send the JWT for protected upload and delete requests.
- Select the endpoint from the authenticated role; never trust a role value supplied by the browser.
- Do not put filesystem paths, storage keys, or tokens in query strings.
- Escape file names when rendering them; do not render server-provided names as HTML.
- Treat client-side size/type checks as UX validation, not a security boundary.
- Download attachments only through the documented controller; never request the raw `filePath`.
- Never send the local filename as a URL or query parameter; only send the browser `File` in `FormData`.
- Do not use the frontend `accept` attribute as proof of file type.

## 9. Definition of Done

- [ ] API calls live in the existing service layer and reuse its base URL/authentication handling.
- [ ] The multipart field is named `file`, and code does not manually set `Content-Type`.
- [ ] Customer uploads use the customer endpoint; staff uploads use the ticket endpoint.
- [ ] File name, formatted size, progress/state, retry, and removal controls are accessible.
- [ ] Completed files update the resource without a full-page reload.
- [ ] Partial failure does not discard successful uploads or failed-file retry state.
- [ ] Avatar previews are image-only and object URLs are revoked.
- [ ] Raw `filePath` values are never rendered or exposed.
- [ ] Tests cover successful upload, validation, `401`, `403`, `404`, `413`, and server/network failure.

## 10. Manual Test Checklist

- Upload one valid file and several valid files.
- Reject an empty file and, after a server limit is documented, a file over that limit.
- Reject a non-image avatar while allowing backend-supported ticket attachment types.
- Retry one failed upload while keeping successful uploads visible.
- Verify the upload button cannot submit twice.
- Verify `401` and `403` responses produce distinct messages.
- Refresh ticket details after upload and confirm metadata is preserved.
- Delete an attachment and confirm the list updates after `204 No Content`.
- Test keyboard selection, screen-reader labels, and a mobile viewport.

## 11. Backend Gaps to Track Separately

These are backend follow-ups, not workarounds to implement in React:

- Review and enforce ticket-level authorization for attachment download before allowing broader access.
- Configure explicit `spring.servlet.multipart.max-file-size` and `max-request-size` values and publish them to the frontend contract.
- Define an attachment MIME-type/extension policy and server-side content inspection if required.
- Review attachment upload/delete ownership enforcement for every role before exposing delete controls broadly.
