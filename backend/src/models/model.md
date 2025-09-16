# Models

Các model trong dự án này mô tả cấu trúc và ràng buộc dữ liệu (schema), ánh xạ với các bảng/collection và hỗ trợ xác định quan hệ giữa các thực thể. Chúng giúp tách biệt mô hình dữ liệu khỏi các tầng khác của ứng dụng, đảm bảo code dễ bảo trì và kiểm thử.

## Danh sách các model

- **UserModel**  
   Đại diện dữ liệu người dùng: thuộc tính, validation, quan hệ.

- **MentorModel**  
   Đại diện dữ liệu mentor: thông tin hồ sơ, chuyên môn, quan hệ.

- **SessionModel**  
   Đại diện các phiên làm việc giữa mentor và mentee.

- **ReviewModel**  
   Đại diện các phản hồi/đánh giá từ người dùng.

## Cách sử dụng

Các model được sử dụng trong service/controller hoặc lớp truy cập dữ liệu để thao tác với dữ liệu. Ví dụ:

```ts
// Ví dụ (giả định ORM hỗ trợ các phương thức truy vấn trên Model)
const user = await UserModel.findById(userId);
```

## Thêm model mới

1. Tạo file mới trong thư mục `models`.
2. Định nghĩa schema/TypeScript interface hoặc class cho model (thuộc tính, ràng buộc, quan hệ).
3. Export model để sử dụng ở service hoặc controller.

## Lưu ý

- Model chỉ nên mô tả cấu trúc dữ liệu và hành vi gắn liền với thực thể; tránh chứa business logic hoặc logic truy xuất dữ liệu phức tạp.
- Sử dụng tên thuộc tính và phương thức rõ ràng, dễ hiểu.
- Định nghĩa kiểu dữ liệu trả về và tham số bằng TypeScript để tăng tính an toàn và dễ bảo trì.
