# Hướng dẫn đồng bộ sang repo cá nhân

## Mục đích

File này lưu ngữ cảnh để một session khác có thể đồng bộ code hiện tại của
restaurant-service và search-service sang repo cá nhân mà không cần đọc lại
cuộc hội thoại trước. Đây là hướng dẫn thao tác, không phải yêu cầu tự động
commit/push khi chưa được người dùng yêu cầu đồng bộ.

## Repo nguồn và repo đích

| Thành phần | Giá trị |
| --- | --- |
| Repo nhóm / nguồn | `https://github.com/hungday12334/meal-go-platform.git` |
| Repo cá nhân / đích | `https://github.com/iamphongtm/meal-go.git` |
| Branch nguồn trước đây | `search-service` — luôn kiểm tra branch hiện tại |
| Branch đích | `update/search-restaurant` |
| Branch mặc định repo cá nhân | `main` — không tự merge hoặc push trực tiếp vào đây |

Repo cá nhân là repo riêng có cấu trúc cũ, không phải bản mirror của repo nhóm.
Chỉ đồng bộ phạm vi được yêu cầu, không làm hai repo giống hệt nhau.

## Ánh xạ thư mục

| Repo nhóm | Repo cá nhân |
| --- | --- |
| `services/restaurant-service/` | `restaurant-service/` |
| `services/search-service/` | `search-service/` |
| `common-web/` | `common-web/` — chỉ cập nhật nếu cần cho hai service |
| `infra/debezium/restaurant-outbox.json` | Cùng đường dẫn |
| Các Compose fragment liên quan | `infra/restaurant/`, `infra/search/`, `infra/kafka/`, `infra/debezium/` |

File hướng dẫn này có thể được chuyển sang
`search-service/PERSONAL_REPO_SYNC.md` trong repo cá nhân.

### Khác biệt phải giữ ở repo cá nhân

- Hai service nằm ở thư mục gốc, không nằm dưới `services/`.
- `restaurant-service/pom.xml` và `search-service/pom.xml` dùng
  `<relativePath>../pom.xml</relativePath>`.
- Maven parent ở gốc chỉ khai báo các module đã được đưa sang:
  `common-web`, `restaurant-service`, `search-service`.
- Không sao chép nguyên danh sách module của repo nhóm vì nhiều module không
  tồn tại trong cấu trúc cá nhân.
- Compose dùng project name `mealgo-personal`, không đặt `container_name` cố
  định; giữ port có thể cấu hình qua `.env` và chỉ bind localhost.
- Giữ các điều chỉnh build/test, tài liệu và code riêng ở repo cá nhân trừ khi
  đã đối chiếu và xác định cần thay đổi.

## Mốc đồng bộ đã thực hiện

Thông tin dưới đây là lịch sử tham khảo, không thay thế việc kiểm tra remote:

| Commit trên repo cá nhân | Nội dung |
| --- | --- |
| `c98cbb0` | Maven parent và common-web |
| `ae05904` | Snapshot restaurant-service: domain, menu, outbox, tọa độ, migration |
| `69307f7` | Search-service: Kafka catalog indexing và geo-point location |
| `daf6c6f` | Docker Compose cho hạ tầng và script đăng ký connector |

Snapshot nguồn ban đầu dựa trên commit `c96d6e9` của repo nhóm, **có bao gồm
thay đổi local chưa commit tại thời điểm đồng bộ**. Không được giả định rằng
chỉ so sánh commit đó sẽ bao quát đầy đủ những gì đã chuyển.

Trong lần đồng bộ ban đầu, đã chỉnh riêng ở bản cá nhân:

- Thêm tọa độ vào payload/assertion của `RestaurantControllerTest` để phù hợp
  validation bắt buộc latitude/longitude.
- Pin `maven-compiler-plugin` phiên bản `3.15.0`.
- Loại bỏ các comment có chuỗi giống AWS credentials khỏi cấu hình.
- Thêm `docs/search-restaurant-sync.md`, `infra/README.md`, `.env.example` và
  `infra/debezium/register-connector.ps1`.

## Quy trình cho session thực hiện đồng bộ

### 1. Kiểm tra trước khi thay đổi

1. Đọc file này và kiểm tra `AGENTS.md` áp dụng nếu có.
2. Kiểm tra branch, remote, status, diff và staged files của repo nguồn.
3. Xác nhận những thay đổi nào thuộc hai service và những phụ thuộc cần thiết.
4. Clone/fetch repo cá nhân vào một thư mục riêng; xác minh URL remote chính xác.
5. Checkout branch `update/search-restaurant` từ remote; nếu đã có bản clone
   local, kiểm tra thay đổi local trước khi cập nhật và chỉ fast-forward khi phù hợp.
6. Đối chiếu nội dung hiện tại của hai repo, không dựa riêng vào tên commit.

Bản clone dùng ở session ban đầu nằm tại:

```text
C:\Users\Acer\AppData\Local\Temp\opencode\meal-go-personal-sync
```

Đây chỉ là vị trí tham khảo. Không giả định thư mục còn tồn tại hoặc sạch;
kiểm tra trước khi dùng. Có thể tạo bản clone riêng mới nếu cần.

### 2. Đồng bộ có chọn lọc

- Mang theo thay đổi code, migration, resource và test liên quan.
- Chỉ cập nhật hạ tầng, parent hoặc common-web khi cần thiết và sau khi đối chiếu.
- Giữ ánh xạ thư mục và các điều chỉnh dành riêng cho repo cá nhân.
- Không copy chồng cả thư mục một cách mù quáng. Kiểm tra file bị xóa/đổi tên,
  vì repo đích có thể có thay đổi riêng cần giữ lại.
- Nếu gặp thay đổi xung đột hoặc không rõ có thuộc phạm vi hay không, báo rõ và
  hỏi người dùng thay vì tự loại bỏ code.
- Không đưa `.env`, credentials, token, private key, `target/`, log, `.idea/`
  hoặc dữ liệu/volume Docker vào commit. `.env.example` chỉ chứa giá trị mẫu.
- Kiểm tra secrets trong diff trước khi push. Nếu phát hiện key có vẻ thật,
  không sao chép key và khuyến nghị người dùng thu hồi/đổi key.

### 3. Kiểm thử

Yêu cầu JDK 21. Tại gốc repo cá nhân, chạy:

```powershell
.\restaurant-service\mvnw.cmd -f pom.xml test
git diff --check
```

Nếu thay đổi Compose/script:

```powershell
docker compose config --quiet
docker compose config --services
```

Kiểm tra cú pháp PowerShell cho script được sửa nếu có. Không khởi chạy thêm
stack hoặc đăng ký/cập nhật connector trên môi trường đang chạy khi người dùng
chưa yêu cầu; có thể xung đột port hoặc làm thay đổi luồng CDC.

Ở mốc đồng bộ ban đầu, 21 test restaurant-service pass; search-service compile
thành công nhưng chưa có test tự động. Luôn báo kết quả thực tế của lần chạy mới,
không sử dụng kết quả cũ để khẳng định code mới đã được kiểm thử.

### 4. Commit và push

- Chỉ stage các file đã kiểm tra trong phạm vi đồng bộ.
- Chia commit theo thay đổi thực tế, dùng message mô tả đúng nội dung.
- Giữ thời gian commit thực tế; không backdate, giả lịch sử hoạt động, hoặc gán
  công lao của người khác cho người dùng. Với snapshot import, ghi nguồn rõ ràng.
- Khi người dùng đã yêu cầu đồng bộ và push, push vào
  `origin/update/search-restaurant` của **repo cá nhân**.
- Không force-push, amend commit đã push, xóa branch, hoặc merge vào `main`
  nếu chưa được người dùng cho phép rõ ràng.
- Nếu remote có cập nhật không mong đợi, fetch và kiểm tra trước, không ghi đè.
- Cuối cùng báo commit hash, URL branch/compare, file chính thay đổi, kết quả
  kiểm thử và các giới hạn chưa kiểm chứng.
- Nếu không có khác biệt cần đồng bộ, báo không có thay đổi; không tạo commit rỗng.

## Giới hạn an toàn đối với repo nhóm

- Không đổi `origin` của repo nhóm thành repo cá nhân.
- Không commit, stash, reset, discard hoặc thay đổi staged files của repo nhóm
  chỉ để phục vụ đồng bộ sang cá nhân.
- Không xóa/sửa thay đổi local của người dùng.
- Không push sang repo nhóm trong tác vụ đồng bộ cá nhân.
- Không dùng `git reset --hard`, `git checkout --`, `git clean` hoặc xóa rộng
  thư mục để làm sạch dữ liệu đang có.
- Không công khai code repo nhóm nếu quyền chia sẻ chưa được xác nhận; nếu
  trạng thái truy cập hoặc phạm vi code thay đổi, kiểm tra lại trước khi xuất bản.

## Lưu ý chức năng và runtime

- Event nhà hàng chứa `latitude`, `longitude`.
- `RestaurantDocumentMapper` chuyển chúng thành
  `location: { "lat": ..., "lon": ... }` khi có đủ cả hai tọa độ.
- Mapping Elasticsearch yêu cầu `location` kiểu `geo_point`.
- Initializer hiện tại không tự migrate mapping của index đã có alias.
  Index cũ cần cập nhật mapping riêng, sau đó backfill/replay có kiểm soát.
- Restart Kafka consumer không tự đọc lại event đã commit offset.
- Với database cũ, cần kiểm tra tương thích Flyway và sao lưu trước khi migrate;
  không tự baseline hoặc xóa database.
- Compose đã hỗ trợ cả hai ứng dụng Java qua shared Dockerfile; có thể chạy
  IDE/Maven thay thế nhưng không chạy hai bản cùng port. Đường dẫn service trong
  Compose phải theo cấu trúc riêng của từng repo.
- Chỉ đăng ký connector sau khi restaurant-service đã tạo `public.outbox_events`;
  kiểm tra cả connector/task ở trạng thái `RUNNING`.
- Không chạy `docker compose down -v` nếu chưa được yêu cầu xóa dữ liệu.

## Câu yêu cầu mẫu cho session sau

> Đọc `services/search-service/PERSONAL_REPO_SYNC.md`, kiểm tra thay đổi hiện tại,
> rồi đồng bộ restaurant-service và search-service sang repo cá nhân theo hướng
> dẫn. Chạy kiểm thử và push vào `update/search-restaurant`, không merge vào main
> và không thay đổi working tree/staging của repo nhóm.
