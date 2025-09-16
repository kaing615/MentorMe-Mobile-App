# Mock - Dữ liệu giả

## Mục đích
Cung cấp sample data để phát triển UI và testing mà không cần phụ thuộc vào backend.

## Files chính
- `MockData.kt` - Object chứa tất cả mock data

## Mock Data Sets

### `mockUsers`
- **Chứa**: 2 users mẫu (1 mentor, 1 student)
- **Dữ liệu**: Thông tin cơ bản như name, email, role, avatar, bio
- **Mục đích**: Test authentication flow và user profiles

### `mockMentors` 
- **Chứa**: 2 mentors với profile đầy đủ
- **Dữ liệu**: 
  - Expertise: Android/Kotlin/Compose, iOS/Swift/React Native
  - Experience: 8-10+ years
  - Rates: $75-85/hour
  - Ratings: 4.8-4.9 stars
  - Languages, timezone, availability status
- **Mục đích**: Test mentor discovery, filtering, booking flow

### `mockBookings`
- **Chứa**: 1 booking mẫu
- **Dữ liệu**: 
  - Topic: "Android Architecture Patterns"
  - Status: CONFIRMED
  - Scheduled time, duration, meeting URL
  - Liên kết với mock mentor và student
- **Mục đích**: Test booking management, status tracking

## Cách sử dụng

### Trong UI Development
```kotlin
// Sử dụng trong screens để hiển thị data
LazyColumn {
    items(MockData.mockMentors) { mentor ->
        MentorCard(mentor = mentor)
    }
}
```

### Trong Repository (Development Mode)
```kotlin
// Có thể switch giữa API và mock data
if (isDevelopmentMode) {
    return Result.Success(MockData.mockMentors)
} else {
    return api.getMentors()
}
```

## Lợi ích
1. **Offline Development**: Phát triển UI không cần backend running
2. **Consistent Test Data**: Dữ liệu ổn định cho testing
3. **Quick Prototyping**: Nhanh chóng demo features
4. **Edge Cases**: Có thể tạo data cho các trường hợp đặc biệt
5. **Performance**: Không cần network calls khi develop UI

## Mở rộng
- Thêm mock data cho notifications
- Mock error states và edge cases
- Mock pagination responses
- Mock real-time chat messages
