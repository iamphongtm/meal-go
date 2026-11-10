# Partner application review

## Contract

Account identity remains owned by auth-service. Customer accounts submit restaurant applications
using the existing `POST /restaurants` request; the owner is the authenticated account, never a
client-supplied owner ID. Successful submission returns the existing 201 response.

| Method | Route | Access | Behaviour |
|---|---|---|---|
| GET | `/restaurants/me/application` | Authenticated owner | Full application, review status and rejection reason; 404 if absent |
| PUT | `/restaurants/{id}/application` | Authenticated owner | Resubmit a rejected application using the same body as registration |
| GET | `/admin/restaurant-applications?status=PENDING&page=0&size=20` | ADMIN | Page of applications; page >= 0, size 1..100; stable submittedAt/id order |
| GET | `/admin/restaurant-applications/{id}` | ADMIN | Application detail |
| PATCH | `/admin/restaurant-applications/{id}/review` | ADMIN | Review a pending application |

Review bodies:

```json
{"decision":"APPROVED"}
```

```json
{"decision":"REJECTED","reason":"Please correct the restaurant address."}
```

Rejection reason must contain 1..1000 characters after trimming (request size is also capped at
1000). `PENDING` is not a valid decision. Reviewing an already reviewed application returns 409.
Only rejected applications can be resubmitted. Resubmission clears review metadata, sets a new
submission timestamp, and resets operating status to CLOSED.

Approval is separate from operating status. Controller `@PreAuthorize` annotations enforce roles;
management routes require RESTAURANT; ownership
and APPROVED are also checked by persistence-backed management operations. Unapproved public
menu and item routes return 404. Opening a pending/rejected restaurant is forbidden.

## Events and integration boundary

Approval locks the restaurant aggregate, updates it through domain behaviour and persists these
outbox events in the same transaction:

- `RestaurantApproved`: eventId, eventType, occurredAt, restaurantId, ownerId, reviewedBy.
  Intended for auth-service to add RESTAURANT while preserving CUSTOMER.
- `RestaurantCreated`: existing customer search publication contract, deferred from submission
  until approval. Rejected/pending new applications are not published to search.

This change does **not** implement outbox delivery/retry or the auth consumer. Existing auth
self-upgrade must still be restricted separately. Database approval alone does not grant a role.

The existing service trusts gateway-injected account/role headers. It must not be exposed directly
to untrusted clients; the gateway must authenticate and overwrite identity headers. Gateway routing
for `/admin/restaurant-applications/**` must be verified during integration.

## Deployment and existing data

Migration V8 deliberately marks existing restaurants PENDING rather than assuming historical
approval. Existing restaurants will lose access to management/public menu until reviewed.
Review the rollout and existing data **before deployment**; do not silently auto-approve records.
Existing external search documents are not removed by this migration and require reconciliation
by the search-service integration/operations rollout. This service cannot hide those documents.

Migration is PostgreSQL-specific and has not been applied to a live database by this change.

## History

- 2026-11-10: Implemented restaurant-side application review, resubmission, access restrictions,
  approval outbox contract and deferred search publication. Auth/frontend integration remains pending.
