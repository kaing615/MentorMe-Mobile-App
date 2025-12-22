package com.mentorme.app.domain.usecase.profile

data class UpdateProfileParams(
    val phone: String? = null,
    val location: String? = null,
    val bio: String? = null,
    val languages: List<String>? = null,
    val skills: List<String>? = null,
    val avatarPath:  String? = null
)