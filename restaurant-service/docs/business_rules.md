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

## Cập nhật địa chỉ nhà hàng

| ID | Quy tắc | Trạng thái / nơi thực thi |
|---|---|---|
| BR-RESTAURANT-ADDRESS-01 | Chỉ RESTAURANT là chủ quán được cập nhật; không tồn tại hoặc sai chủ trả 404. Quán phải APPROVED và CLOSED; chưa đóng trả 409, chưa duyệt dùng lỗi not-approved 403. Giữ nguyên duyệt và trạng thái, không tự mở bán. | Đã có code: RestaurantController, UpdateRestaurantAddressUseCaseImpl, Restaurant.updateAddress |
| BR-RESTAURANT-ADDRESS-02 | Bắt buộc provinceCode/wardCode nguyên dương, ward tồn tại thuộc tỉnh, addressLine không trắng tối đa 255 ký tự, trim khi lưu; latitude/longitude hữu hạn trong [-90,90]/[-180,180]. Cập nhật cả nhóm, không geocode hay xác minh ranh giới. | Đã có code; không kiểm tra đơn đang xử lý, không sửa địa chỉ đơn cũ |
| BR-RESTAURANT-ADDRESS-03 | Tải aggregate có khóa; cập nhật và outbox RestaurantAddressUpdated cùng transaction. Không đổi dữ liệu thì trả 200 không phát event. Snapshot mang name/status/addressLine/wardCode/latitude/longitude; Search xử lý bất đồng bộ, xem docs/business_rules.md của search-service. | Đã có code; cần hạ tầng outbox/CDC/Kafka đang hoạt động để đồng bộ thực tế |
| BR-RESTAURANT-ADDRESS-04 | Merchant đọc hồ sơ/menu từ API thật; chỉ bật sửa địa chỉ khi quán CLOSED và APPROVED. Form dùng tìm kiếm Place VietMap, ánh xạ tên hành chính khi khớp duy nhất, cho chọn danh mục thủ công và yêu cầu xác nhận địa chỉ. PATCH đủ năm trường; giữ dữ liệu khi lỗi, cập nhật trang bằng response khi thành công. Không hiển thị số liệu đánh giá/lịch hoạt động/bản đồ giả. | Đã có code: RestaurantPage, RestaurantAddressDialog, partnerApi; browser kiểm thử bằng fixture |

## Ảnh đại diện nhà hàng

| ID | Quy tắc | Trạng thái / nơi thực thi |
|---|---|---|
| BR-RESTAURANT-AVATAR-01 | PATCH `/restaurants/{restaurantId}/avatar` nhận avatarUrl; chỉ role RESTAURANT là chủ quán APPROVED được cập nhật, không yêu cầu CLOSED. Sai chủ/không tồn tại trả 404; chưa duyệt trả 403. | Đã có code: UpdateAvatarUseCaseImpl, Restaurant.updateAvatar |
| BR-RESTAURANT-AVATAR-02 | URL bắt buộc, tối đa 2048 ký tự trước trim, HTTP/HTTPS có host và không chứa credentials. Không tải hoặc xác minh nội dung ảnh; client upload trước rồi gửi URL. Không hỗ trợ xóa bằng null/rỗng, không thêm ảnh bìa, không xóa object cũ. | Đã có code: domain validation, request DTO |
| BR-RESTAURANT-AVATAR-03 | Lưu restaurants.avatar_url nullable; bản ghi cũ giữ null. Khóa aggregate và cập nhật qua domain trong transaction; URL không đổi trả 200 không ghi lại. Giữ trạng thái duyệt/vận hành. GET /restaurants/me và menu/menu/tree trả avatarUrl; chưa đồng bộ avatar sang Search. | Đã có code và migration V9; chưa chạy DB thực tế |
| BR-RESTAURANT-AVATAR-04 | POST /restaurants/{id}/uploads dùng GenerateUploadUrlUseCase chung, kiểm tra role RESTAURANT và sở hữu, trả presigned URL cho upload S3. Key mới restaurants/{id}/images; endpoint menu-items/uploads giữ tương thích. Client upload xong mới PATCH avatar. | Đã có code backend; frontend Merchant không thuộc repo cá nhân |

## Lịch sử cập nhật

- 2026-11-11: Import backend từ repo nhóm commit f415e9151a0cce814ddc42f37fed86606007acb4: avatar, upload dùng chung, menu validation và refactor bỏ var/toán tử ba ngôi. Giữ cấu trúc Maven, cấu hình môi trường, test và tài liệu riêng của repo cá nhân. Không import frontend/Auth.

- 2026-11-11: Import backend Restaurant từ snapshot repo nhóm. Các mục Merchant/Admin phía trên là tài liệu tham chiếu, không có frontend hoặc Auth trong đợt đồng bộ này. Search chưa nhận event riêng khi quán đổi trạng thái mở/đóng; cần xử lý trước khi coi trạng thái index là thời gian thực.

- 2026-11-11: Cập nhật layout trang Nhà hàng Merchant, bỏ dữ liệu mock của trang, nối API cập nhật địa chỉ và toast; hỗ trợ khung mobile và điều hướng thu gọn. Kiểm thử browser fixture ở 360/768/1280; chưa kiểm thử PATCH vào DB thật.

- 2026-11-11: Thêm PATCH /restaurants/{restaurantId}/address: chủ quán APPROVED và CLOSED cập nhật trọn bộ địa chỉ/tọa độ; giữ duyệt và trạng thái vận hành, không sửa đơn cũ. Lưu aggregate và RestaurantAddressUpdated trong cùng transaction; Search đồng bộ bất đồng bộ.

- 2026-11-11: Merchant đối chiếu boundary tỉnh type=0 và phường/xã type=2 của Place/Autocomplete v4 với tên danh mục nội bộ, chuẩn hóa dấu/tiền tố hành chính. Chỉ tự điền mã hệ thống khi khớp duy nhất, tải phường theo tỉnh đã khớp; thiếu/không khớp/lỗi giữ chọn thủ công, không dùng ID VietMap làm mã nội bộ. Giữ trường tỉnh/phường để kiểm tra và sửa.

- 2026-11-11: Thay bản đồ trong form đăng ký bằng tìm địa chỉ Autocomplete v4 và lấy tọa độ Place v4; giữ danh mục tỉnh/phường backend và payload đăng ký hiện có. Dùng VIETMAP_SEARCH_API_KEY server-side cho Vite dev proxy, chưa tích hợp production proxy.

- 2026-11-11: Merchant dev proxy /vietmap đến https://maps.vietmap.vn; MapLibre chuyển tài nguyên style/source/tile/glyph/sprite cùng host qua proxy để tránh CORS local. Không tắt bảo mật trình duyệt; production vẫn cần VietMap cho phép origin hoặc reverse proxy tương ứng (Vite dev proxy không áp dụng production).

- 2026-11-11: Merchant bỏ tiền tố /backend, cấu hình VITE_API_BASE_URL=/ và BACKEND_URL trong .env; Vite proxy trực tiếp /auth-service và /restaurant-service đến API Gateway (mặc định http://localhost:8765), giữ nguyên đường dẫn. Không thay đổi endpoint hoặc quyền backend; proxy chỉ phục vụ dev, production cần reverse proxy tương ứng hoặc API base URL phù hợp.

- 2026-11-11: Frontend Merchant hỗ trợ chọn/kéo ghim trên nền bản đồ VietMap (MapLibre), cấu hình public web key VITE_VIETMAP_MAP_API_KEY. GPS chỉ được yêu cầu khi bấm nút; không tự ghi đè địa chỉ theo tọa độ. Yêu cầu xác nhận ghim trước khi gửi; thay đổi địa chỉ hành chính, số nhà/tên đường hoặc tọa độ hủy xác nhận cũ. Giữ nguyên API và payload đăng ký; chưa geocode/reverse-geocode hoặc kiểm tra địa giới tự động.

- 2026-11-10: Cập nhật Merchant đăng nhập CUSTOMER/RESTAURANT, điều hướng theo quyền và hồ sơ hiện có, hiển thị trạng thái xét duyệt và gửi lại hồ sơ bị từ chối. Không thay đổi quyền backend hoặc tự cấp role từ frontend.

- 2026-11-10: Frontend Admin gọi duyệt hồ sơ rồi gọi auth-service cấp quyền bằng ownerId; hai bước không nguyên tử. Có thử cấp quyền lại và bộ lọc APPROVED để phục hồi sau reload. RestaurantApproved vẫn được lưu nhưng chưa có consumer; không triển khai thêm luồng cấp quyền tự động trong mốc này.

- 2026-11-10: Bổ sung latitude/longitude vào thông tin nhà hàng của hai API menu công khai để trang chi tiết khách hàng có thể đọc tọa độ quán; không thay đổi dữ liệu hay quyền truy cập.

- 2026-11-10: Thêm frontend/mealgo-admin dùng API thật: đăng nhập ADMIN, danh sách PENDING, duyệt trực tiếp và từ chối có lý do; không có màn hình chi tiết. Thông báo thành công chỉ xác nhận hồ sơ đã được xử lý, không khẳng định quyền tài khoản đã được cấp.

- 2026-11-10: Triển khai hồ sơ, xét duyệt admin, gửi lại, chặn quản lý/mở bán trước duyệt và ghi outbox. Chưa sửa auth-service/frontend, chưa xác nhận migration runtime. Chuyển các quy tắc BR-PARTNER-* đã ghi nhầm trong tài liệu delivery-service về đúng service.
