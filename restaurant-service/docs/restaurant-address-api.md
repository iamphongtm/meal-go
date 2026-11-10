# Update restaurant address

Gateway: `PATCH /restaurant-service/restaurants/{restaurantId}/address`.

Service: `PATCH /restaurants/{restaurantId}/address`.

Requires a valid authenticated RESTAURANT owner. Identity comes from the trusted gateway; clients must not supply ownerId.

```json
{
  "provinceCode": 79,
  "wardCode": 12345,
  "addressLine": "123 Nguyễn Trãi",
  "latitude": 10.762622,
  "longitude": 106.660172
}
```

Administrative codes above are illustrative; use codes returned by the geography APIs. All five fields are required. Province and ward codes must be positive, and the ward must exist and belong to the province. Address must be nonblank and at most 255 characters before trimming. Coordinates must be finite and within latitude [-90, 90] and longitude [-180, 180]. This does not verify geographical boundaries or the actual business location.

Response 200 contains restaurantId and all five normalized address fields. Restaurant must be APPROVED and CLOSED. Approval and operating status are preserved. Repeating an unchanged address returns 200 without a new event.

Errors: 400 invalid input or ward/province mismatch; 401 unauthenticated; 403 insufficient role or restaurant not approved; 404 missing restaurant, another owner's restaurant, or missing ward; 409 restaurant not CLOSED.

The aggregate is loaded with a write lock. Persistence and RestaurantAddressUpdated outbox append share a transaction. Search receives the snapshot asynchronously and preserves menuItems; 200 does not imply the search index has refreshed. No order data is modified and no active-order check is performed.

Rules: BR-RESTAURANT-ADDRESS-01..03 in business_rules.md.
