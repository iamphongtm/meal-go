# Business rules — Restaurant Service

## Đăng ký và xét duyệt đối tác

| ID | Quy tắc | Trạng thái / nơi thực thi |
|---|---|---|
| BR-PARTNER-01 | Dùng chung account; khi được duyệt bổ sung RESTAURANT và giữ CUSTOMER. | Đã thống nhất; restaurant-service ghi RestaurantApproved; consumer cấp quyền auth chưa triển khai trong mốc này |
| BR-PARTNER-02 | Hồ sơ quán mới PENDING, trạng thái vận hành CLOSED; owner lấy từ danh tính đăng nhập. Một owner chỉ có một quán đang tồn tại. | Đã có code: RegisterRestaurantUseCaseImpl, Restaurant |
| BR-PARTNER-03 | Chỉ ADMIN duyệt/từ chối PENDING; lưu reviewedBy/reviewedAt; từ chối bắt buộc lý do 1..1000 ký tự sau trim, request tối đa 1000 ký tự. Duyệt lặp trả 409; quyết định PENDING không hợp lệ. | Đã có code: RestaurantApplicationUseCase, Restaurant.review; khóa ghi aggregate |
| BR-PARTNER-04 | Chỉ chủ hồ sơ REJECTED được sửa/gửi lại; giữ ID và owner, chuyển PENDING, xóa thông tin xét duyệt, cập nhật submittedAt và đặt CLOSED. Sai chủ sở hữu trả 404. | Đã có code: resubmit |
| BR-PARTNER-05 | Controller dùng @PreAuthorize; quản lý yêu cầu RESTAURANT, kiểm tra sở hữu và APPROVED; hồ sơ chưa duyệt không mở bán, public menu/item trả 404. | Đã có code; header danh tính dựa trên gateway tin cậy |
| BR-PARTNER-06 | Duyệt và hai outbox event RestaurantApproved + RestaurantCreated cùng transaction; chỉ công bố RestaurantCreated sau duyệt. RestaurantApproved mang ownerId phục vụ cấp quyền giữ CUSTOMER. | Đã có code phía ghi; gửi/retry/consumer auth chưa triển khai |
| BR-PARTNER-07 | Danh sách admin lọc trạng thái, mặc định PENDING; page mặc định 0, >=0; size mặc định 20, 1..100; sort submittedAt/id tăng dần. | Đã có code: admin controller/query adapter |
| BR-PARTNER-08 | V8 không suy đoán đã duyệt: bản ghi cũ thành PENDING; phải rà soát trước triển khai. Search documents cũ cần đối soát riêng, migration không xóa index ngoài service. | Đã có migration, chưa chạy database thực tế |
| BR-PARTNER-09 | Merchant cho CUSTOMER đăng nhập đăng ký đối tác. RESTAURANT có quán vào trang quản lý; tài khoản chưa có quyền gọi GET /restaurants/me/application: chỉ lỗi restaurant.not-found 404 được xem là chưa có hồ sơ; PENDING xem chờ duyệt, REJECTED chỉnh sửa/gửi lại cùng hồ sơ, APPROVED chưa có role hướng dẫn đăng nhập lại/liên hệ hỗ trợ. Lỗi tải không được coi là chưa đăng ký. | Đã có code frontend Merchant; dùng API hiện có, áp dụng cả khôi phục phiên và chặn truy cập trực tiếp trang quản lý |

Chi tiết API, sự kiện và giới hạn tích hợp: [partner-applications.md](partner-applications.md).

## Vị trí cửa hàng trên Merchant

| ID | Quy tắc | Trạng thái / nơi thực thi |
|---|---|---|
| BR-PARTNER-LOCATION-01 | Chủ quán tìm địa chỉ thật qua Autocomplete v4 display_type=1, chọn ref_id rồi gọi Place v4 lấy tọa độ hợp lệ; không dùng bản đồ/GPS trong form đăng ký. FE yêu cầu xác nhận địa chỉ đã chọn khớp cửa hàng và tỉnh/phường trong hồ sơ; đổi nội dung địa chỉ hủy xác nhận, đổi truy vấn tìm kiếm xóa lựa chọn/tọa độ. Không dùng ID hành chính VietMap làm mã hệ thống; ánh xạ tên chuẩn hóa khi khớp duy nhất và tải phường theo tỉnh, không khớp thì chọn thủ công. Không tự kiểm tra địa giới tọa độ. | Đã có code frontend: RestaurantAddressSearch, PartnerRegisterPage, addressMapping |
| BR-PARTNER-LOCATION-02 | Dev proxy chỉ cho Autocomplete/Place v4, thêm VIETMAP_SEARCH_API_KEY phía Vite server, không gửi key trong request trình duyệt. Có debounce, hủy request cũ, validate response và lỗi phục hồi; không dùng mock/tọa độ giả. Production cần proxy backend bảo vệ key và quota tương ứng. | Đã có code dev proxy; chưa triển khai proxy production |

## Thực đơn công khai

| ID | Quy tắc | Trạng thái / nơi thực thi |
|---|---|---|
| BR-MENU-LOCATION-01 | GET `/restaurants/{id}/menu` và `/restaurants/{id}/menu/tree` trả thêm `restaurant.latitude`, `restaurant.longitude` từ tọa độ quán đã lưu, không suy ra từ địa chỉ hoặc vị trí khách. Giá trị thiếu giữ nguyên `null`, không thay bằng 0 hoặc vị trí mặc định. | Đã có code: RestaurantMenuQueryAdapter, RestaurantMenuResult, RestaurantMenuTreeResult |
| BR-MENU-LOCATION-02 | Bổ sung tọa độ không thay đổi điều kiện công bố: chỉ quán APPROVED có menu công khai; quán đã duyệt nhưng đóng cửa vẫn xem được với `acceptingOrders=false`. | Đã có code; giữ nguyên BR-PARTNER-05 |

## Lịch sử cập nhật

- 2026-11-11: Import partner application review, resubmission, approval guards and menu coordinates from the team snapshot. Frontend behavior above is reference only; frontend and Auth are not included in this repository update.
