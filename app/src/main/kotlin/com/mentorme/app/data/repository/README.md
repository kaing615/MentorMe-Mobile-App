# Repository - Lớp truy cập dữ liệu

## Mục đích
Triển khai Repository pattern để trừu tượng hóa nguồn dữ liệu và cung cấp API nhất quán cho ViewModels.

## Files chính
- `Repositories.kt` - Các interface repository
- `impl/AuthRepositoryImpl.kt` - Implementation cho authentication
- `impl/RepositoryImplementations.kt` - Implementation cho Mentor và Booking repositories

## Repository Interfaces

### `AuthRepository`
- **Chức năng**: Quản lý authentication và user session
- **Methods**:
  - `login()` - Đăng nhập với email/password
  - `register()` - Đăng ký tài khoản mới
  - `logout()` - Đăng xuất và xóa token
  - `getCurrentUser()` - Lấy thông tin user hiện tại
  - `getToken()` - Flow để observe token changes
  - `saveToken()` / `clearToken()` - Quản lý token local

### `MentorRepository`
- **Chức năng**: Quản lý thông tin mentors
- **Methods**:
  - `getMentors()` - Lấy danh sách mentors với filters
  - `getMentorById()` - Chi tiết mentor
  - `getMentorAvailability()` - Lịch trống của mentor

### `BookingRepository`  
- **Chức năng**: Quản lý booking lifecycle
- **Methods**:
  - `createBooking()` - Tạo booking mới
  - `getBookings()` - Danh sách bookings với filters
  - `getBookingById()` - Chi tiết booking
  - `updateBooking()` - Cập nhật status booking
  - `rateBooking()` - Đánh giá sau session

## Repository Implementations

### `AuthRepositoryImpl`
- Inject `MentorMeApi` và `DataStoreManager`
- Gọi API và lưu token vào DataStore khi login thành công
- Xử lý logout: gọi API và xóa token local
- Error handling với Result wrapper

### `MentorRepositoryImpl` & `BookingRepositoryImpl`
- Inject `MentorMeApi` để gọi REST endpoints
- Transform Response<T> thành Result<T>
- Xử lý lỗi network và API errors
- Return type-safe data cho UI layer

## Lợi ích Repository Pattern
1. **Separation of Concerns**: Tách biệt data logic khỏi UI
2. **Testability**: Dễ mock cho unit tests
3. **Flexibility**: Có thể thêm caching, offline support
4. **Consistency**: API nhất quán cho tất cả ViewModels
5. **Error Handling**: Centralized error handling với Result wrapper
