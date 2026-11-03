# Customer menu API

These read-only endpoints use the restaurant database, not Elasticsearch.
Paths follow the service's existing `/restaurants` prefix (no `/api` prefix).
No `X-Owner-Id` header is required. Current service security permits all requests.

## Get restaurant menu

`GET /restaurants/{restaurantId}/menu`

```json
{
  "restaurant": {
    "restaurantId": "7ef4c4ad-00f7-4947-8395-81f972dce8b3",
    "name": "Bún bò Huế",
    "addressLine": "12 Nguyễn Tri Phương",
    "acceptingOrders": true,
    "unavailableReason": null
  },
  "categories": [
    {
      "categoryId": "11111111-1111-1111-1111-111111111111",
      "name": "Món chính",
      "displayOrder": 1,
      "items": [
        {
          "menuItemId": "22222222-2222-2222-2222-222222222222",
          "name": "Bún bò đặc biệt",
          "description": null,
          "basePrice": 65000,
          "imageUrl": null,
          "available": true,
          "hasOptions": true
        }
      ]
    }
  ]
}
```

- Only active categories and non-inactive items are included; sold-out items have
  `available: false`.
- Categories and items follow display order, then ID.
- Closed restaurants remain viewable: `acceptingOrders: false`,
  `unavailableReason: "RESTAURANT_CLOSED"`.
- Menu item availability here reflects the item status only. The UI must also
  check restaurant `acceptingOrders` and fetch item detail before adding options.
- `hasOptions` indicates an assigned active option group, not guaranteed orderability.
- Empty menus return `categories: []`. Categories may have `items: []`.
- Unknown/soft-deleted restaurants return 404 (Problem Detail).

## Get item detail and options

`GET /restaurants/{restaurantId}/menu-items/{menuItemId}`

```json
{
  "menuItemId": "22222222-2222-2222-2222-222222222222",
  "restaurantId": "7ef4c4ad-00f7-4947-8395-81f972dce8b3",
  "name": "Bún bò đặc biệt",
  "description": null,
  "basePrice": 65000,
  "currency": "VND",
  "imageUrl": null,
  "available": true,
  "optionGroups": [
    {
      "optionGroupId": "33333333-3333-3333-3333-333333333333",
      "name": "Kích cỡ",
      "minSelections": 1,
      "maxSelections": 1,
      "displayOrder": 1,
      "options": [
        {
          "optionId": "44444444-4444-4444-4444-444444444444",
          "name": "Lớn",
          "additionalPrice": 15000,
          "available": true,
          "displayOrder": 1
        }
      ]
    }
  ]
}
```

- The item must belong to the restaurant in the URL. Wrong ownership, hidden
  category/item or missing item returns 404.
- Only active option groups and non-inactive options are exposed. Sold-out
  options are visible but unavailable.
- Prices and selection limits come from the item's group assignment, not a
  global default. Options without an item-specific price record are omitted.
- Detail `available` checks restaurant opening status, item status, and whether
  required option groups have enough selectable options. Missing/inactive
  required groups make the item unavailable.
- Each option can be selected at most once per serving; validate selections
  against `minSelections` / `maxSelections`.
- A dish without groups returns `optionGroups: []`.
- Displayed total = `(basePrice + sum(selected additionalPrice)) * quantity`.
  The order backend must validate IDs, availability, selection limits and prices
  again; these GET responses do not reserve availability or lock a price.

Malformed UUID path parameters return 400. These APIs do not create carts/orders.

## Tests

With Java 21 configured, from `services/restaurant-service`:

```powershell
.\mvnw.cmd test
```
