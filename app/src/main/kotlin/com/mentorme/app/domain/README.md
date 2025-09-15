# Domain - Lớp nghiệp vụ

## Mục đích
Chứa logic nghiệp vụ phức tạp và use cases độc lập với Android framework.

## Cấu trúc thư mục

### `usecase/` - Use Cases
- **Vai trò**: Xử lý logic nghiệp vụ phức tạp
- **Chức năng**: Kết hợp nhiều repository, validate business rules
- **Ví dụ**: BookMentorSessionUseCase, ProcessPaymentUseCase

## Khi nào sử dụng
- Logic nghiệp vụ phức tạp cần nhiều bước
- Kết hợp dữ liệu từ nhiều repository
- Business rules validation
- Workflow có nhiều bước

## Trạng thái hiện tại
Folder đã được tạo nhưng chưa có use cases cụ thể. Sẽ thêm khi logic nghiệp vụ phức tạp hơn.
