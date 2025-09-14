## 4. `middlewares/`

**Chứa các middleware cho Express (kiểm tra xác thực JWT, phân quyền, log, bắt lỗi toàn cục, ...).**

- Dùng cho logic trung gian chạy trước khi controller được gọi.
- Viết chuẩn middleware của ExpressJS, sử dụng TypeScript để đảm bảo kiểu dữ liệu và tăng tính an toàn.

**Ví dụ:**

- `auth.middleware.ts`
- `role.middleware.ts`
- `error.middleware.ts`
