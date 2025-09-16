# Model - Mô hình dữ liệu

## Mục đích
Định nghĩa các data class chính của ứng dụng MentorMe, đại diện cho các thực thể nghiệp vụ.

## Files chính
- `Models.kt` - Tất cả data models và enums

## Data Models

### `User`
- **Mục đích**: Thông tin người dùng cơ bản
- **Fields**: id, email, name, role, avatar, phone, bio, timestamps
- **Đặc điểm**: Hỗ trợ cả Student và Mentor roles

### `Mentor` 
- **Mục đích**: Thông tin chi tiết mentor
- **Fields**: userId, expertise, experience, hourlyRate, rating, totalBookings, availability
- **Quan hệ**: Liên kết với User qua userId

### `Booking`
- **Mục đích**: Thông tin phiên mentoring
- **Fields**: mentorId, studentId, scheduledAt, duration, status, topic, notes, rating
- **Lifecycle**: PENDING → CONFIRMED → IN_PROGRESS → COMPLETED/CANCELLED

### Enums
- `UserRole`: STUDENT, MENTOR, ADMIN
- `BookingStatus`: PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED

## Đặc điểm kỹ thuật
- Sử dụng `@SerializedName` cho JSON mapping
- Immutable data classes với `val`
- Nullable fields cho optional data
- Type-safe với Kotlin enums
