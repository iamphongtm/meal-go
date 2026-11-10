# Business rules — Search Service

## Đồng bộ địa chỉ nhà hàng

| ID | Quy tắc | Trạng thái / nơi thực thi |
|---|---|---|
| BR-SEARCH-ADDRESS-01 | Nhận RestaurantAddressUpdated từ outbox của restaurant-service; kiểm tra restaurantId khớp aggregateId và dùng snapshot name/status/addressLine/latitude/longitude để cập nhật document và geo location, giữ nguyên menuItems. | Đã có code: CatalogEventType, CatalogIndexServiceImpl, ElasticsearchRestaurantIndexRepository |
| BR-SEARCH-ADDRESS-02 | Dùng restaurantUpdatedAt hiện có để bỏ qua event trùng hoặc cũ. Kết quả tìm kiếm nhất quán cuối cùng sau consumer và Elasticsearch refresh, không cam kết cập nhật ngay khi PATCH trả 200. | Đã có code; tái sử dụng cơ chế timestamp hiện có |

Hợp đồng nguồn: [restaurant-service business rules](../../restaurant-service/docs/business_rules.md), BR-RESTAURANT-ADDRESS-01..03.

Giới hạn tích hợp: cập nhật trạng thái mở/đóng hiện chưa phát event riêng. Sau khi đổi địa chỉ lúc CLOSED rồi mở lại, Search có thể còn giữ trạng thái CLOSED; không coi snapshot Search là trạng thái vận hành thời gian thực.

## Lịch sử cập nhật

- 2026-11-11: Bổ sung nhận RestaurantAddressUpdated, tái sử dụng upsert snapshot nhà hàng và bảo toàn menu.
