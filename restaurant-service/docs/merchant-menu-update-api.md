# Merchant menu item updates

Both endpoints require the authenticated restaurant owner. Unknown or foreign
restaurants, items and categories return the existing not-found business errors.

## PUT /restaurants/{restaurantId}/menu-items/{menuItemId}

```json
{
  "categoryId": "uuid",
  "name": "Milk tea",
  "description": "Traditional milk tea",
  "basePrice": 35000,
  "imageUrl": "https://cdn.example.com/tea.webp"
}
```

Returns 204. Replaces editable details only, preserving status and option-group
assignments/prices. A null imageUrl removes the image. Name is required (255
characters maximum); description is optional (255 maximum). Price must be
nonnegative with at most two decimal places. The category must belong to the
same restaurant. Moving categories appends the dish to the new category.
Updates load the item with a write lock before saving.

## PATCH /restaurants/{restaurantId}/menu-items/{menuItemId}/status

```json
{ "status": "SOLD_OUT" }
```

Returns 204. Supported statuses: ACTIVE (selling), SOLD_OUT (temporarily out),
INACTIVE (hidden from the customer menu). Detail fields and option groups remain
unchanged. The merchant UI toggles ACTIVE/SOLD_OUT so the existing customer-menu
listing can still display the dish and allow the merchant to reactivate it.

Existing image uploads are reused. Merchant API calls use the gateway prefix
`/restaurant-service`. No database migration is required for these endpoints.
