package com.mentorme.app.data.mock

import com.mentorme.app.data.model.*

object MockData {

    val mockUsers = listOf(
        User(
            id = "1",
            email = "john.mentor@example.com",
            name = "John Smith",
            role = UserRole.MENTOR,
            avatar = "https://example.com/avatar1.jpg",
            phone = "+1234567890",
            bio = "Experienced software engineer with 10+ years in mobile development",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        ),
        User(
            id = "2",
            email = "alice.student@example.com",
            name = "Alice Johnson",
            role = UserRole.STUDENT,
            avatar = "https://example.com/avatar2.jpg",
            phone = "+1234567891",
            bio = "Computer science student passionate about mobile development",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )
    )

    val mockMentors = listOf(
        Mentor(
            id = "1",
            userId = "1",
            user = mockUsers[0],
            expertise = listOf("Android", "Kotlin", "Compose", "Architecture"),
            experience = "10+ years in mobile development at Google and Meta",
            hourlyRate = 85.0,
            rating = 4.9,
            totalBookings = 127,
            isAvailable = true,
            languages = listOf("English", "Vietnamese"),
            timezone = "UTC+7",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        ),
        Mentor(
            id = "2",
            userId = "3",
            user = User(
                id = "3",
                email = "sarah.mentor@example.com",
                name = "Sarah Wilson",
                role = UserRole.MENTOR,
                avatar = "https://example.com/avatar3.jpg",
                phone = "+1234567892",
                bio = "iOS and React Native expert",
                createdAt = "2024-01-01T00:00:00Z",
                updatedAt = "2024-01-01T00:00:00Z"
            ),
            expertise = listOf("iOS", "Swift", "React Native", "UI/UX"),
            experience = "8 years developing mobile apps for startups and enterprises",
            hourlyRate = 75.0,
            rating = 4.8,
            totalBookings = 89,
            isAvailable = true,
            languages = listOf("English", "Spanish"),
            timezone = "UTC-5",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )
    )

    val mockBookings = listOf(
        Booking(
            id = "1",
            mentorId = "1",
            studentId = "2",
            mentor = mockMentors[0],
            student = mockUsers[1],
            scheduledAt = "2024-12-20T14:00:00Z",
            duration = 60,
            status = BookingStatus.CONFIRMED,
            topic = "Android Architecture Patterns",
            notes = "Want to learn about MVVM and Clean Architecture",
            meetingUrl = "https://meet.google.com/abc-defg-hij",
            rating = null,
            feedback = null,
            createdAt = "2024-12-15T00:00:00Z",
            updatedAt = "2024-12-15T00:00:00Z"
        )
    )
}
