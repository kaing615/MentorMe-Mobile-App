# Remote - API từ xa

## Mục đích
Định nghĩa interface REST API để giao tiếp với backend MentorMe server.

## Files chính
- `MentorMeApi.kt` - Retrofit interface với tất cả API endpoints

## API Endpoints

### Authentication APIs
```kotlin
POST /auth/login          // Đăng nhập
POST /auth/register       // Đăng ký
POST /auth/logout         // Đăng xuất  
GET  /auth/me            // Lấy thông tin user hiện tại
```

### Mentor APIs
```kotlin
GET  /mentors                    // Danh sách mentors (có filter, pagination)
GET  /mentors/{id}              // Chi tiết mentor
GET  /mentors/{id}/availability // Lịch trống của mentor
```

### Booking APIs
```kotlin
POST /bookings           // Tạo booking mới
GET  /bookings          // Danh sách bookings (có filter, pagination)  
GET  /bookings/{id}     // Chi tiết booking
PUT  /bookings/{id}     // Cập nhật booking
POST /bookings/{id}/rate // Đánh giá booking
```

### Message APIs
```kotlin
GET  /messages/{bookingId}  // Lấy tin nhắn của booking
POST /messages             // Gửi tin nhắn mới
```

## Đặc điểm kỹ thuật
- Sử dụng Retrofit annotations (@GET, @POST, @PUT...)
- Hỗ trợ query parameters với @Query
- Request body với @Body
- Path parameters với @Path
- Async với Coroutines (suspend functions)
- Response<T> wrapper để xử lý HTTP status codes
- Base URL: `http://10.0.2.2:3000/api/` (Android emulator)

## Authentication
- Tự động inject Bearer token qua AuthInterceptor
- Token được lưu trong DataStore và inject vào headers
