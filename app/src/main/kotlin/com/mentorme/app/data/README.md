# Data - Lớp dữ liệu

## Mục đích
Xử lý tất cả các thao tác dữ liệu: gọi API, lưu trữ local, chuyển đổi dữ liệu.

## Cấu trúc thư mục

### `model/` - Mô hình dữ liệu
- **Vai trò**: Định nghĩa các đối tượng chính của app
- **Files**: User, Mentor, Booking, enums
- **Chức năng**: Cấu trúc dữ liệu type-safe, JSON serialization

### `dto/` - Data Transfer Objects
- **Vai trò**: Đối tượng request/response cho API
- **Files**: LoginRequest, AuthResponse, BookingRequest...
- **Chức năng**: Tách biệt API contract với business models

### `remote/` - API từ xa
- **Vai trò**: Interface gọi REST API
- **Files**: MentorMeApi với tất cả endpoints
- **Chức năng**: Auth, mentor, booking, chat API calls

### `repository/` - Repository pattern
- **Vai trò**: Trừu tượng hóa nguồn dữ liệu
- **Files**: Interface + implementation cho Auth, Mentor, Booking
- **Chức năng**: Gọi API, xử lý lỗi, quản lý token

### `mock/` - Dữ liệu giả
- **Vai trò**: Sample data cho development và testing
- **Files**: MockData với users, mentors, bookings mẫu
- **Chức năng**: Phát triển UI không cần backend
