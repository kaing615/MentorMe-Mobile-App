package com.mentorme.app.ui.mentors

import com.mentorme.app.core.model.Mentor

val sampleFeaturedMentors = listOf(
    Mentor(
        id = "m1",
        fullName = "Nguyễn An",
        title = "Senior Software Engineer",
        avatarUrl = null,
        bio = "Chuyên React, System Design, hướng nghiệp sang SWE tại Big Tech.",
        skills = listOf("React", "System Design", "Career"),
        languages = listOf("Tiếng Việt", "English"),
        experience = "8+ năm",
        hourlyRate = 40,
        rating = 4.9,
        totalReviews = 128,
        verified = true,
        isOnline = true
    ),
    Mentor(
        id = "m2",
        fullName = "Trần Minh",
        title = "Product Manager",
        avatarUrl = null,
        bio = "PM tại startup Series B, mentoring chuyển ngành từ Marketing sang PM.",
        skills = listOf("Product Strategy", "Roadmap", "Career"),
        languages = listOf("Tiếng Việt"),
        experience = "6+ năm",
        hourlyRate = 35,
        rating = 4.8,
        totalReviews = 96,
        verified = true,
        isOnline = true
    ),
    Mentor(
        id = "m3",
        fullName = "Lê Lan",
        title = "UX Designer",
        avatarUrl = null,
        bio = "Thiết kế trải nghiệm, Portfolio review, phỏng vấn UX.",
        skills = listOf("UX", "Portfolio", "Interview"),
        languages = listOf("English"),
        experience = "7+ năm",
        hourlyRate = 30,
        rating = 4.9,
        totalReviews = 110,
        verified = false,
        isOnline = false
    )
)
