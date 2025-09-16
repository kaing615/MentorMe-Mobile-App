# Repositories

Các repository trong dự án này chịu trách nhiệm truy xuất và thao tác dữ liệu từ các nguồn lưu trữ (database, API, v.v.). Chúng giúp tách biệt logic truy cập dữ liệu khỏi các tầng khác của ứng dụng, đảm bảo code dễ bảo trì và kiểm thử.

## Danh sách các repository

- **UserRepository**  
   Quản lý dữ liệu người dùng: tạo, đọc, cập nhật, xóa thông tin người dùng.

- **MentorRepository**  
   Quản lý dữ liệu mentor: truy xuất thông tin mentor, cập nhật profile mentor.

- **SessionRepository**  
   Quản lý các phiên làm việc giữa mentor và mentee.

- **ReviewRepository**  
   Lưu trữ và truy xuất các phản hồi từ người dùng.

## Cách sử dụng

Các repository được sử dụng trong các service để thực hiện các thao tác với dữ liệu. Ví dụ:

```ts
const user = await userRepository.findById(userId);
```

## Thêm repository mới

1. Tạo file mới trong thư mục `repositories`.
2. Định nghĩa các phương thức cần thiết cho repository bằng TypeScript interface hoặc class.
3. Export repository để sử dụng ở các service hoặc controller.

## Lưu ý

- Repository chỉ nên chứa logic truy xuất dữ liệu, không chứa business logic.
- Sử dụng các phương thức rõ ràng, dễ hiểu.
- Nên định nghĩa kiểu dữ liệu trả về và tham số bằng TypeScript để tăng tính an toàn và dễ bảo trì.
