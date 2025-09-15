# UI - Lớp giao diện

## Mục đích
Chứa tất cả các màn hình và component UI của ứng dụng MentorMe, được xây dựng bằng Jetpack Compose.

## Cấu trúc thư mục

### `navigation/` - Điều hướng
- **Vai trò**: Quản lý navigation và routing
- **Files**: Screen.kt (định nghĩa routes), MentorMeNavigation.kt
- **Chức năng**: Bottom navigation, screen transitions, deep links

### `common/` - Component dùng chung
- **Vai trò**: UI components tái sử dụng
- **Files**: MentorCard, ButtonXL, Loading components
- **Chức năng**: Standardize UI elements, consistent design

### `auth/` - Xác thực
- **Vai trò**: Màn hình đăng nhập và đăng ký
- **Files**: LoginScreen, RegisterScreen
- **Chức năng**: Authentication flow, form validation

### `booking/` - Đặt lịch
- **Vai trò**: Quản lý booking sessions
- **Files**: BookingsScreen, BookingDetail, RatingDialog
- **Chức năng**: Xem lịch đã đặt, đánh giá session, join meeting

### `calendar/` - Lịch
- **Vai trò**: Hiển thị và quản lý lịch trình
- **Chức năng**: Calendar view, time slot selection, availability

### `chat/` - Tin nhắn
- **Vai trò**: Giao diện chat mentor-student
- **Files**: MessagesScreen, ChatBubble
- **Chức năng**: Real-time messaging, conversation history

### `dashboard/` - Bảng điều khiển
- **Vai trò**: Dashboard cho mentor
- **Files**: MentorDashboard, StatsModal
- **Chức năng**: Thống kê, quản lý profile mentor

### `home/` - Trang chủ
- **Vai trò**: Màn hình chính của app
- **Files**: HomeScreen với hero section, quick actions
- **Chức năng**: Featured mentors, recent bookings, shortcuts

### `layout/` - Layout chung
- **Vai trò**: Các component layout dùng chung
- **Files**: BottomNavigation, Header
- **Chức năng**: App bar, navigation structure

### `mentors/` - Mentor
- **Vai trò**: Browse và xem chi tiết mentor
- **Files**: MentorsScreen, MentorDetailScreen, Filters
- **Chức năng**: Search mentors, view profiles, book sessions

### `notifications/` - Thông báo
- **Vai trò**: Quản lý thông báo
- **Chức năng**: Push notifications, in-app notifications

### `profile/` - Hồ sơ
- **Vai trò**: Quản lý profile người dùng
- **Files**: ProfileScreen, SettingsScreen
- **Chức năng**: Edit profile, settings, logout

### `videocall/` - Video call
- **Vai trò**: Gọi video với mentor
- **Chức năng**: WebRTC integration, video conferencing
