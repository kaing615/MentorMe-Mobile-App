## 3. `handlers/`

**Chứa các hàm/tệp handler cho các logic dùng chung hoặc các tác vụ phụ trợ nhỏ, thường là validate request, chuẩn hóa response, ...**

- Được import và sử dụng trong các controller hoặc middleware khác.
- Thường gặp: validate dữ liệu đầu vào, custom response trả về cho client.

**Ví dụ:**

- `request.handler.ts` (kiểm tra validate dữ liệu)
- `response.handler.ts` (chuẩn hóa response trả về)
