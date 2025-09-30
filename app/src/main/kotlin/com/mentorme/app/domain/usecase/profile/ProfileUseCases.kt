package com.mentorme.app.domain.usecase.profile

data class UpdateProfileParams(
    val phone: String? = null,
    val jobTitle: String? = null,
    val location: String? = null,
    val category: String? = null,
    val bio: String? = null,
    val skills: List<String>? = null,
    val experience: String? = null,
    val headline: String? = null,
    val mentorReason: String? = null,
    val greatestAchievement: String? = null,
    val introVideo: String? = null,
    val description: String? = null,
    val goal: String? = null,
    val education: String? = null,
    val languages: List<String>? = null,
    val links: LinksUpsert? = null,     // xem chú ý về backend bên dưới
    val price: Double? = null,
    val avatarPath: String? = null,     // file device
    val avatarUrl: String? = null       // URL direct image
)

data class LinksUpsert(
    val website: String? = null,
    val twitter: String? = null,
    val linkedin: String? = null,
    val github: String? = null,
    val youtube: String? = null,
    val facebook: String? = null
)
