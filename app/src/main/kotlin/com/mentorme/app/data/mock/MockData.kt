// app/src/main/java/com/mentorme/app/data/mock/MockData.kt
package com.mentorme.app.data.mock

import com.mentorme.app.data.model.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object MockData {

    // Helper: chuyển Date bên web -> ISO string cố định
    private fun d(y: Int, m: Int, day: Int) =
        "%04d-%02d-%02dT00:00:00Z".format(y, m, day)
    private fun t(hoursAgo: Int): Long =
        System.currentTimeMillis() - (hoursAgo * 60L * 60L * 1000L)
    
    // ===== Dynamic Mock Bookings for development =====
    
    /**
     * Get mock bookings with dynamic date/time for mentor dashboard
     * Returns: 1 live session (happening now) + 1 upcoming session (later today)
     */
    fun getMockLiveBookingsForMentor(): List<Booking> {
        val today = LocalDate.now()
        val now = LocalTime.now()
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val zone = ZoneId.systemDefault()
        
        // Live session: started 15 mins ago, ends in 45 mins
        val liveStartTime = now.minusMinutes(15)
        val liveEndTime = now.plusMinutes(45)
        val liveStartIso = today.atTime(liveStartTime).atZone(zone).toInstant().toString()
        val liveEndIso = today.atTime(liveEndTime).atZone(zone).toInstant().toString()
        
        // Upcoming session: starts in 2 hours
        val upcomingStartTime = now.plusHours(2)
        val upcomingEndTime = now.plusHours(3)
        val upcomingStartIso = today.atTime(upcomingStartTime).atZone(zone).toInstant().toString()
        val upcomingEndIso = today.atTime(upcomingEndTime).atZone(zone).toInstant().toString()
        
        return listOf(
            // Live session - happening right now
            Booking(
                id = "mock-live-001",
                menteeId = "mentee-user-001",
                mentorId = "current-mentor",
                date = today.toString(),
                startTime = timeFormatter.format(liveStartTime),
                endTime = timeFormatter.format(liveEndTime),
                startTimeIso = liveStartIso,
                endTimeIso = liveEndIso,
                status = BookingStatus.CONFIRMED,
                price = 75.0,
                topic = "Frontend Development Career Path",
                notes = "Tư vấn về career path trong Frontend Development",
                createdAt = d(2026, 1, 1),
                menteeFullName = "Nguyễn Văn An",
                mentee = BookingUserSummary(
                    id = "mentee-user-001",
                    fullName = "Nguyễn Văn An",
                    avatar = null,
                    role = UserRole.MENTEE
                )
            ),
            // Upcoming session - in 2 hours
            Booking(
                id = "mock-upcoming-002",
                menteeId = "mentee-user-002",
                mentorId = "current-mentor",
                date = today.toString(),
                startTime = timeFormatter.format(upcomingStartTime),
                endTime = timeFormatter.format(upcomingEndTime),
                startTimeIso = upcomingStartIso,
                endTimeIso = upcomingEndIso,
                status = BookingStatus.CONFIRMED,
                price = 85.0,
                topic = "System Design Interview Preparation",
                notes = "Mock interview và review thiết kế hệ thống",
                createdAt = d(2026, 1, 1),
                menteeFullName = "Trần Thị Bình",
                mentee = BookingUserSummary(
                    id = "mentee-user-002",
                    fullName = "Trần Thị Bình",
                    avatar = null,
                    role = UserRole.MENTEE
                )
            )
        )
    }


    // ===== Current user (mentee) — map từ mockCurrentUser (web)
    val mockCurrentUser: User = User(
        id = "current-user",
        email = "user@example.com",
        fullName = "Nguyễn Văn User",
        avatar = null,
        role = UserRole.MENTEE,
        createdAt = d(2024, 1, 1)
    )

    // ===== Mentors — map 1:1 từ mockMentors (web)
    val mockMentors: List<Mentor> = listOf(
        Mentor(
            id = "1",
            email = "john.doe@example.com",
            fullName = "John Doe",
            avatar = "https://images.unsplash.com/photo-1543282949-ffbf6a0f263c?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwcm9mZXNzaW9uYWwlMjBtZW50b3IlMjB0ZWFjaGluZyUyMHN0dWRlbnR8ZW58MXx8fHwxNzU3NzQ0NDExfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
            role = UserRole.MENTOR,
            createdAt = d(2023, 1, 15),
            bio = "Senior Software Engineer với 8+ năm kinh nghiệm trong phát triển web và mobile apps. Đã làm việc tại các công ty công nghệ hàng đầu và startup. Chuyên về React, Node.js, và system design.",
            skills = listOf("React", "Node.js", "TypeScript", "System Design", "Leadership"),
            hourlyRate = 75.0,
            rating = 4.9,
            totalReviews = 127,
            availability = emptyList(),
            verified = true,
            experience = "8+ năm",
            education = "Computer Science, Stanford University",
            languages = listOf("Tiếng Việt", "English")
        ),
        Mentor(
            id = "2",
            email = "sarah.wilson@example.com",
            fullName = "Sarah Wilson",
            avatar = "https://images.unsplash.com/photo-1629507313712-f21468afdf2e?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwcm9mZXNzaW9uYWwlMjBmZW1hbGUlMjBwb3J0cmFpdHxlbnwxfHx8fDE3NTc3NTM3NjJ8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
            role = UserRole.MENTOR,
            createdAt = d(2023, 2, 20),
            bio = "Product Manager tại Google với 6 năm kinh nghiệm phát triển sản phẩm digital. Launch nhiều sản phẩm phục vụ hàng triệu người dùng. Chuyên về product strategy và user research.",
            skills = listOf("Product Management", "User Research", "Data Analysis", "Strategy", "Agile"),
            hourlyRate = 85.0,
            rating = 4.8,
            totalReviews = 89,
            availability = emptyList(),
            verified = true,
            experience = "6+ năm",
            education = "MBA, Harvard Business School",
            languages = listOf("English", "Tiếng Việt")
        ),
        Mentor(
            id = "3",
            email = "david.chen@example.com",
            fullName = "David Chen",
            avatar = "https://images.unsplash.com/photo-1556157382-97eda2d62296?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwcm9mZXNzaW9uYWwlMjBtYW4lMjBwb3J0cmFpdHxlbnwxfHx8fDE3NTc3NTM4MDB8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
            role = UserRole.MENTOR,
            createdAt = d(2023, 3, 10),
            bio = "Data Scientist tại Microsoft, mạnh về machine learning và AI. PhD Computer Science, kinh nghiệm build & deploy ML models ở production scale.",
            skills = listOf("Machine Learning", "Python", "Data Science", "AI", "Statistics"),
            hourlyRate = 90.0,
            rating = 4.9,
            totalReviews = 156,
            availability = emptyList(),
            verified = true,
            experience = "7 năm",
            education = "PhD Computer Science, MIT",
            languages = listOf("English", "中文", "Tiếng Việt")
        ),
        Mentor(
            id = "4",
            email = "maria.garcia@example.com",
            fullName = "Maria Garcia",
            avatar = "https://images.unsplash.com/photo-1581065178026-390bc4e78dad?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwcm9mZXNzaW9uYWwlMjB3b21hbiUyMHBvcnRyYWl0fGVufDF8fHx8MTc1Nzc1MjAxOXww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
            role = UserRole.MENTOR,
            createdAt = d(2023, 4, 5),
            bio = "Marketing Director 10+ năm kinh nghiệm digital marketing & brand management. Từng làm với Nike, Apple. Expert social media & content strategy.",
            skills = listOf("Digital Marketing", "Social Media", "Content Strategy", "Brand Management", "Analytics"),
            hourlyRate = 70.0,
            rating = 4.7,
            totalReviews = 64,
            availability = emptyList(),
            verified = true,
            experience = "10+ năm",
            education = "Marketing, UC Berkeley",
            languages = listOf("English", "Español", "Português")
        ),
        Mentor(
            id = "5",
            email = "alex.kim@example.com",
            fullName = "Alex Kim",
            avatar = "https://images.unsplash.com/photo-1739298061757-7a3339cee982?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxidXNpbmVzcyUyMHByb2Zlc3Npb25hbCUyMGhlYWRzaG90fGVufDF8fHx8MTc1NzY4NTg1M3ww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
            role = UserRole.MENTOR,
            createdAt = d(2023, 5, 15),
            bio = "UX/UI Designer tại Airbnb, 5+ năm design thinking, prototyping, user testing. Background psychology & HCI.",
            skills = listOf("UX Design", "UI Design", "Figma", "Prototyping", "User Research"),
            hourlyRate = 65.0,
            rating = 4.8,
            totalReviews = 94,
            availability = emptyList(),
            verified = true,
            experience = "5 năm",
            education = "Design, RISD",
            languages = listOf("English", "한국어", "Tiếng Việt")
        ),
        Mentor(
            id = "6",
            email = "emily.brown@example.com",
            fullName = "Emily Brown",
            avatar = "https://images.unsplash.com/photo-1615702669705-0d3002c6801c?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxjb3Jwb3JhdGUlMjBleGVjdXRpdmUlMjBwb3J0cmFpdHxlbnwxfHx8fDE3NTc2NzM2NDV8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
            role = UserRole.MENTOR,
            createdAt = d(2023, 6, 20),
            bio = "Financial Analyst tại Goldman Sachs, chuyên investment banking & financial modeling. CFA charterholder.",
            skills = listOf("Finance", "Investment Banking", "Financial Modeling", "Valuation", "Excel"),
            hourlyRate = 95.0,
            rating = 4.9,
            totalReviews = 78,
            availability = emptyList(),
            verified = true,
            experience = "6 năm",
            education = "Finance, Wharton",
            languages = listOf("English", "Français")
        )
    )

    // ===== Mock Bookings - Dữ liệu mẫu cho Calendar Screen =====
    val mockBookings: List<Booking> = listOf(
        // Booking sắp tới (hôm nay + các ngày tới)
        Booking(
            id = "booking-1",
            menteeId = "current-user",
            mentorId = "1",
            date = "2025-09-17", // Hôm nay
            startTime = "14:00",
            endTime = "15:00",
            status = BookingStatus.CONFIRMED,
            price = 75.0,
            notes = "Tư vấn về career path trong Software Engineering",
            createdAt = d(2025, 9, 15)
        ),
        Booking(
            id = "booking-2",
            menteeId = "current-user",
            mentorId = "2",
            date = "2025-09-18", // Ngày mai
            startTime = "10:00",
            endTime = "11:30",
            status = BookingStatus.CONFIRMED,
            price = 127.50, // 1.5h * 85$/h
            notes = "Product strategy và roadmap planning",
            createdAt = d(2025, 9, 16)
        ),
        Booking(
            id = "booking-3",
            menteeId = "current-user",
            mentorId = "3",
            date = "2025-09-20",
            startTime = "16:00",
            endTime = "17:00",
            status = BookingStatus.CONFIRMED,
            price = 65.0,
            notes = "Review portfolio và feedback thiết kế",
            createdAt = d(2025, 9, 17)
        ),

        // Booking đang chờ duyệt
        Booking(
            id = "booking-4",
            menteeId = "current-user",
            mentorId = "1",
            date = "2025-09-25",
            startTime = "09:00",
            endTime = "10:30",
            status = BookingStatus.PENDING_MENTOR,
            price = 112.50, // 1.5h * 75$/h
            notes = "System design interview preparation",
            createdAt = d(2025, 9, 17)
        ),

        // Booking đã hoàn thành (quá khứ)
        Booking(
            id = "booking-5",
            menteeId = "current-user",
            mentorId = "2",
            date = "2025-09-15",
            startTime = "14:00",
            endTime = "15:00",
            status = BookingStatus.COMPLETED,
            price = 85.0,
            notes = "Thảo luận về product metrics và KPI",
            createdAt = d(2025, 9, 13)
        ),
        Booking(
            id = "booking-6",
            menteeId = "current-user",
            mentorId = "3",
            date = "2025-09-12",
            startTime = "11:00",
            endTime = "12:00",
            status = BookingStatus.COMPLETED,
            price = 65.0,
            notes = "Học cách sử dụng Figma hiệu quả",
            createdAt = d(2025, 9, 10)
        ),

        // Booking đã bị hủy
        Booking(
            id = "booking-7",
            menteeId = "current-user",
            mentorId = "1",
            date = "2025-09-10",
            startTime = "15:00",
            endTime = "16:00",
            status = BookingStatus.CANCELLED,
            price = 75.0,
            notes = "Code review session - đã hủy do lý do cá nhân",
            createdAt = d(2025, 9, 8)
        ),

        // Thêm một số booking khác để test pagination
        Booking(
            id = "booking-8",
            menteeId = "current-user",
            mentorId = "2",
            date = "2025-09-22",
            startTime = "13:00",
            endTime = "14:00",
            status = BookingStatus.CONFIRMED,
            price = 85.0,
            notes = "Coaching session về leadership skills",
            createdAt = d(2025, 9, 17)
        ),
        Booking(
            id = "booking-9",
            menteeId = "current-user",
            mentorId = "1",
            date = "2025-09-08",
            startTime = "10:00",
            endTime = "11:30",
            status = BookingStatus.COMPLETED,
            price = 112.50,
            notes = "Technical interview mock session",
            createdAt = d(2025, 9, 6)
        ),
        Booking(
            id = "booking-10",
            menteeId = "current-user",
            mentorId = "3",
            date = "2025-09-30",
            startTime = "14:30",
            endTime = "15:30",
            status = BookingStatus.PENDING_MENTOR,
            price = 65.0,
            notes = "Design system deep dive",
            createdAt = d(2025, 9, 17)
        )
    )
    // ===== Thêm metadata cho Booking để dựng đúng UI theo Figma =====
    data class BookingUiExtra(
        val topic: String,
        val sessionType: String,      // "video" | "in-person"
        val paymentStatus: String,    // "paid" | "pending" | "refunded"
        val menteeNotes: String? = null
    )

    /** Map id -> extra info (topic, session type, payment, ghi chú) */
    val bookingExtras: Map<String, BookingUiExtra> = mapOf(
        // confirmed / upcoming
        "booking-1" to BookingUiExtra(
            topic = "Career Guidance Session",
            sessionType = "video",
            paymentStatus = "paid",
            menteeNotes = null
        ),
        "booking-2" to BookingUiExtra(
            topic = "Product Strategy & Roadmap",
            sessionType = "video",
            paymentStatus = "paid",
            menteeNotes = null
        ),
        "booking-3" to BookingUiExtra(
            topic = "Portfolio Review & Feedback",
            sessionType = "in-person",
            paymentStatus = "paid",
            menteeNotes = null
        ),

        // pending
        "booking-4" to BookingUiExtra(
            topic = "System Design Interview Preparation",
            sessionType = "in-person",
            paymentStatus = "pending",
            menteeNotes = "Muốn thảo luận về career path và skill development"
        ),

        // completed
        "booking-5" to BookingUiExtra(
            topic = "Product Metrics & KPI Deep Dive",
            sessionType = "video",
            paymentStatus = "paid"
        ),
        "booking-6" to BookingUiExtra(
            topic = "Figma Efficient Workflow",
            sessionType = "video",
            paymentStatus = "paid"
        ),

        // cancelled
        "booking-7" to BookingUiExtra(
            topic = "Code Review Session",
            sessionType = "video",
            paymentStatus = "refunded",
            menteeNotes = "Đã hủy do lý do cá nhân"
        ),

        // more samples
        "booking-8" to BookingUiExtra(
            topic = "Leadership Coaching",
            sessionType = "video",
            paymentStatus = "paid"
        ),
        "booking-9" to BookingUiExtra(
            topic = "Technical Interview Mock",
            sessionType = "video",
            paymentStatus = "paid"
        ),
        "booking-10" to BookingUiExtra(
            topic = "Design System Deep Dive",
            sessionType = "video",
            paymentStatus = "pending"
        )
    )

    /** Helper lấy tên nhanh cho UI */
    fun mentorNameById(id: String): String =
        mockMentors.firstOrNull { it.id == id }?.fullName ?: "Mentor"

    val currentMenteeName: String
        get() = mockCurrentUser.fullName

    // ===== Mock Notifications =====
    val mockNotifications: List<NotificationItem> = listOf(
        NotificationItem(
            title = "Booking request",
            body = "New booking request pending your response.",
            type = NotificationType.BOOKING_PENDING,
            timestamp = t(1),
            read = false,
            deepLink = "booking_detail/booking-4"
        ),
        NotificationItem(
            title = "Booking xác nhận",
            body = "Mentor John Doe đã xác nhận buổi tư vấn của bạn.",
            type = NotificationType.BOOKING_CONFIRMED,
            timestamp = t(2),
            read = false
        ),
        NotificationItem(
            title = "Nhắc lịch 1h",
            body = "Buổi tư vấn với Sarah Wilson sẽ bắt đầu sau 1 giờ nữa.",
            type = NotificationType.BOOKING_REMINDER,
            timestamp = t(5),
            read = false
        ),
        NotificationItem(
            title = "Lịch bị hủy",
            body = "Buổi tư vấn với David Chen đã bị hủy. Vui lòng chọn lịch khác.",
            type = NotificationType.BOOKING_CANCELLED,
            timestamp = t(9),
            read = true
        ),
        NotificationItem(
            title = "Tin nhắn mới",
            body = "Bạn có 1 tin nhắn mới từ mentor.",
            type = NotificationType.MESSAGE,
            timestamp = t(12),
            read = true
        ),
        NotificationItem(
            title = "MentorMe",
            body = "Cập nhật mới: Hỗ trợ đặt lịch nhanh hơn.",
            type = NotificationType.SYSTEM,
            timestamp = t(24),
            read = true
        )
    )

}
