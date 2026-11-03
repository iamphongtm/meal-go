# Restaurant search API

## Nearby restaurants

```http
GET /api/search/restaurants/nearby?latitude=10.7769&longitude=106.7009&radiusKm=5&page=0&size=10
```

- `latitude` and `longitude` are required.
- `radiusKm` defaults to 5 and must be a finite positive number.
- Uses Elasticsearch `geo_distance` with `arc` distance and sorts nearest first.
- `distanceKm` is straight-line surface distance, not road distance or delivery ETA.
- Restaurants without coordinates do not match.
- Does not return the full menu; `matchedMenuItems` is empty.

## Restaurants matching a dish name

```http
GET /api/search/restaurants/by-dish?keyword=phở&page=0&size=10
```

- `keyword` is required, nonblank, and at most 200 characters.
- Searches only `menuItems.name` using a nested match query (all analyzed query terms must match).
- Returns multiple restaurants, each with up to five best-matching dishes in `matchedMenuItems`.
- A restaurant with fewer matches returns only those matches, without unrelated dishes.
- Uses the highest matching dish score to rank restaurants; restaurant ID breaks score ties.
- `distanceKm` is null because this endpoint does not require coordinates.

## Common behavior

- Response fields: `content`, `page`, `size`, `totalElements`, `totalPages`.
- Pagination is by restaurant, not by menu item. Page starts at 0; size defaults to 10 (1–100).
- Requests beyond the default 10,000-result Elasticsearch window return HTTP 400.
- Invalid input returns HTTP 400.
- Placeholder documents created by menu events are excluded until restaurant details arrive.
- Neither endpoint filters restaurant or menu item status; status is returned to the caller.
- Search is near real-time: newly indexed events become searchable after Elasticsearch refresh.
- Endpoints are on search-service directly; API gateway routing is not changed here.

Example dish result:

```json
{
  "content": [
    {
      "restaurantId": "restaurant-123",
      "name": "Phở Hà Nội",
      "status": "OPENING",
      "addressLine": "123 Nguyễn Trãi",
      "distanceKm": null,
      "matchedMenuItems": [
        {
          "menuItemId": "item-123",
          "name": "Phở bò",
          "status": "ACTIVE",
          "basePrice": 50000,
          "imageUrl": null
        }
      ]
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```
