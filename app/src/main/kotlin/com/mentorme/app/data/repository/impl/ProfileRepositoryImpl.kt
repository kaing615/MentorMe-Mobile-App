package com.mentorme.app.data.repository.impl

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.profile.ProfileCreateResponse
import com.mentorme.app.data.dto.profile.MePayload
import com.mentorme.app.data.dto.profile.MeUserDto
import com.mentorme.app.data.dto.profile.ProfileDto
import com.mentorme.app.data.network.api.profile.ProfileApiService
import com.mentorme.app.data.repository.ProfileRepository
import com.mentorme.app.domain.usecase.onboarding.RequiredProfileParams
import com.mentorme.app.domain.usecase.profile.UpdateProfileParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File as JavaFile  // ✅ Alias để tránh conflict
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val api: ProfileApiService
) : ProfileRepository {

    override suspend fun getMe(): AppResult<MePayload> {
        return try {
            val res = api.getMyProfile()

            android.util.Log.d("ProfileRepo", "getMe HTTP code: ${res.code()}")

            if (res.isSuccessful) {
                val profileMePayload = res.body()?.data
                val profileMe = profileMePayload?.profile

                if (profileMe != null) {
                    val mePayload = MePayload(
                        user = profileMe.user?. let { embedded ->
                            MeUserDto(
                                id = embedded.id,
                                email = embedded.email,
                                role = embedded.role,
                                userName = embedded.userName,
                                name = profileMe.fullName,  // Dùng fullName từ profile
                                createdAt = null
                            )
                        },
                        profile = ProfileDto(
                            id = profileMe.id,
                            user = profileMe.user?.id,  // Chuyển object → string id
                            fullName = profileMe.fullName,
                            phone = profileMe.phone,
                            location = profileMe.location,
                            category = profileMe.category,
                            avatarUrl = profileMe.avatarUrl,
                            languages = profileMe.languages,
                            links = profileMe.links,
                            jobTitle = profileMe.jobTitle,
                            experience = profileMe.experience,
                            headline = profileMe.headline,
                            mentorReason = profileMe.mentorReason,
                            greatestAchievement = profileMe.greatestAchievement,
                            bio = profileMe.bio,
                            introVideo = profileMe.introVideo,
                            description = profileMe.description,
                            goal = profileMe.goal,
                            education = profileMe.education,
                            skills = profileMe.skills,
                            profileCompleted = profileMe.profileCompleted
                        )
                    )

                    android.util.Log.d("ProfileRepo", "Mapped fullName: ${mePayload.profile?. fullName}")
                    android.util.Log.d("ProfileRepo", "Mapped phone: ${mePayload.profile?. phone}")
                    android.util. Log.d("ProfileRepo", "Mapped bio: ${mePayload.profile?. bio}")

                    AppResult.success(mePayload)
                } else {
                    AppResult.failure(Exception("Empty profile"))
                }
            } else {
                val errorBody = res.errorBody()?.string()
                AppResult.failure(Exception("HTTP ${res.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e("ProfileRepo", "getMe exception", e)
            AppResult.failure(e)
        }
    }

    override suspend fun updateProfile(
        params: UpdateProfileParams
    ): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val parts = mutableMapOf<String, okhttp3.RequestBody>()

            params.phone?.let {
                parts["phone"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
            }
            params.location?.let {
                parts["location"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
            }
            params.bio?.let {
                parts["bio"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
            }

            params.languages?.let {
                parts["languages"] = it.joinToString(",")
                    .toRequestBody("text/plain".toMediaTypeOrNull())
            }

            params.skills?.let {
                parts["skills"] = it.joinToString(",")
                    .toRequestBody("text/plain".toMediaTypeOrNull())
            }

            // ✅ Dùng JavaFile (alias)
            val avatarPart: MultipartBody.Part?  = params.avatarPath?.let { path ->
                val file = JavaFile(path)
                if (file.exists()) {
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("avatar", file.name, requestFile)
                } else null
            }

            val resp = api.updateProfile(fields = parts, avatar = avatarPart)

            if (resp.isSuccessful) {
                AppResult.success(Unit)
            } else {
                val errorBody = resp.errorBody()?.string()
                AppResult.failure("HTTP ${resp.code()}: $errorBody")
            }
        } catch (e: Exception) {
            AppResult.failure(e.message ?: "Update failed")
        }
    }

    override suspend fun createRequiredProfile(
        params: RequiredProfileParams
    ): AppResult<ProfileCreateResponse> = withContext(Dispatchers.IO) {
        try {
            fun splitCsv(s: String): List<String> =
                s.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            val parts = mutableMapOf<String, okhttp3.RequestBody>()

            parts["fullName"] = params.fullName
                .toRequestBody("text/plain".toMediaTypeOrNull())
            parts["location"] = params.location
                .toRequestBody("text/plain".toMediaTypeOrNull())
            parts["category"] = params.category
                .toRequestBody("text/plain".toMediaTypeOrNull())
            parts["languages"] = splitCsv(params.languages).joinToString(",")
                .toRequestBody("text/plain".toMediaTypeOrNull())
            parts["skills"] = splitCsv(params.skills).joinToString(",")
                .toRequestBody("text/plain".toMediaTypeOrNull())

            params.description?.let {
                parts["description"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
            }
            params.goal?.let {
                parts["goal"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
            }
            params.education?.let {
                parts["education"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
            }
            params.avatarUrl?.let {
                parts["avatarUrl"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
            }
            params.jobTitle?.let {
                parts["jobTitle"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
            }
            params.experience?.let {
                parts["experience"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
            }
            params.headline?.let {
                parts["headline"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
            }
            params.mentorReason?.let {
                parts["mentorReason"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
            }
            params.greatestAchievement?.let {
                parts["greatestAchievement"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
            }
            params.bio?.let {
                parts["bio"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
            }
            params.introVideo?.let {
                parts["introVideo"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
            }

            // ✅ Dùng JavaFile (alias) - QUAN TRỌNG:  đổi từ File thành JavaFile
            val avatarPart: MultipartBody.Part? = params.avatarPath?.let { path ->
                val file = JavaFile(path)  // ⬅️ Đổi từ File thành JavaFile
                if (file.exists()) {
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("avatar", file.name, requestFile)
                } else null
            }

            val resp = api.createRequiredProfileMultipart(
                fields = parts,
                avatar = avatarPart
            )

            if (resp.isSuccessful) {
                resp.body()?.let { AppResult.success(it) }
                    ?: AppResult.failure("Response body is null")
            } else {
                val errorBody = resp.errorBody()?.string()
                AppResult.failure("HTTP ${resp.code()}: $errorBody")
            }
        } catch (e: Exception) {
            AppResult.failure(e.message ?:  "Failed to create profile")
        }
    }
}