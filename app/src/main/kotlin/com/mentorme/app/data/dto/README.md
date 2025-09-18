# DTO - Data Transfer Objects

## Mục đích
Chứa các object dùng cho request/response API, tách biệt với business models.

## Files chính
- `DTOs.kt` - Tất cả request/response objects

## Auth DTOs
### `LoginRequest`
- **Mục đích**: Gửi thông tin đăng nhập
- **Fields**: email, password

### `RegisterRequest`  
- **Mục đích**: Đăng ký tài khoản mới
- **Fields**: email, password, name, role

### `AuthResponse`
- **Mục đích**: Phản hồi sau đăng nhập/đăng ký thành công
- **Fields**: token, user

## Mentor DTOs
### `MentorListResponse`
- **Mục đích**: Response danh sách mentors với pagination
- **Fields**: mentors[], total, page, totalPages

### `AvailabilitySlot`
- **Mục đích**: Thông tin khung thời gian có sẵn của mentor
- **Fields**: date, startTime, endTime, isAvailable

## Booking DTOs
### `CreateBookingRequest`
- **Mục đích**: Tạo booking mới
- **Fields**: mentorId, scheduledAt, duration, topic, notes

### `UpdateBookingRequest`
- **Mục đích**: Cập nhật booking (status, time, notes)
- **Fields**: status, scheduledAt, notes (all optional)

### `BookingListResponse`
- **Mục đích**: Response danh sách bookings với pagination
- **Fields**: bookings[], total, page, totalPages

### `RatingRequest`
- **Mục đích**: Đánh giá session
- **Fields**: rating, feedback

## Message DTOs
### `Message`
- **Mục đích**: Tin nhắn trong chat
- **Fields**: id, bookingId, senderId, sender, content, timestamp, messageType

### `SendMessageRequest`
- **Mục đích**: Gửi tin nhắn mới
- **Fields**: bookingId, content, messageType

## Lợi ích
- Tách biệt API contract với domain models
- Dễ dàng thay đổi API mà không ảnh hưởng business logic
- Type-safe với @SerializedName annotations
- Hỗ trợ API versioning
