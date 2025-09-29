# Core - Hạ tầng chung

## Mục đích
Chứa các thành phần cơ sở hạ tầng và tiện ích dùng chung cho toàn bộ ứng dụng MentorMe.

## Cấu trúc thư mục

### `di/` - Dependency Injection
- **Vai trò**: Cấu hình Hilt để inject dependencies
- **Files**: NetworkModule, RepositoryModule
- **Chức năng**: Quản lý singleton của API, Repository, DataStore

### `network/` - Mạng
- **Vai trò**: Xử lý kết nối API và HTTP
- **Files**: Interceptors cho authentication và headers
- **Chức năng**: Tự động thêm token, log requests

### `datastore/` - Lưu trữ cục bộ  
- **Vai trò**: Quản lý token và preferences người dùng
- **Files**: DataStoreManager
- **Chức năng**: Lưu/đọc token, thông tin user an toàn

### `utils/` - Tiện ích
- **Vai trò**: Các hàm helper dùng chung
- **Files**: DateTimeUtils, ValidationUtils, Result wrapper
- **Chức năng**: Format ngày tháng, validate input, xử lý lỗi

### `designsystem/` - Hệ thống thiết kế
- **Vai trò**: Theme và style chung của app
- **Files**: Theme, Typography, Colors
- **Chức năng**: Màu sắc MentorMe, font chữ, Material 3
