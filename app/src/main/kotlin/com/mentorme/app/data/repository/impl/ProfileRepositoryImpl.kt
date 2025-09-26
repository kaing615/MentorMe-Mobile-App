package com.mentorme.app.data.repository.impl

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.profile.ProfileCreateResponse
import com.mentorme.app.data.network.api.profile.ProfileApiService
import com.mentorme.app.data.repository.ProfileRepository
import com.mentorme.app.domain.usecase.onboarding.RequiredProfileParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val api: ProfileApiService
) : ProfileRepository {

    override suspend fun createRequiredProfile(
        params: RequiredProfileParams
    ): AppResult<ProfileCreateResponse> = withContext(Dispatchers.IO) {
            // tiện hàm convert chuỗi CSV thành list string
            fun splitCsv(s: String): List<String> =
                s.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            // chuẩn bị các part text
            val parts = mutableMapOf<String, okhttp3.RequestBody>()
            parts["location"] = params.location.toRequestBody("text/plain".toMediaTypeOrNull())
            parts["category"] = params.category.toRequestBody("text/plain".toMediaTypeOrNull())
            parts["languages"] = splitCsv(params.languages).joinToString(",")
                .toRequestBody("text/plain".toMediaTypeOrNull())
            parts["skills"] = splitCsv(params.skills).joinToString(",")
                .toRequestBody("text/plain".toMediaTypeOrNull())

            params.description?.let { parts["description"] = it.toRequestBody("text/plain".toMediaTypeOrNull()) }
            params.goal?.let { parts["goal"] = it.toRequestBody("text/plain".toMediaTypeOrNull()) }
            params.education?.let { parts["education"] = it.toRequestBody("text/plain".toMediaTypeOrNull()) }
            params.avatarUrl?.let { parts["avatarUrl"] = it.toRequestBody("text/plain".toMediaTypeOrNull()) }
            params.jobTitle?.let { parts["jobTitle"] = it.toRequestBody("text/plain".toMediaTypeOrNull()) }
            params.experience?.let { parts["experience"] = it.toRequestBody("text/plain".toMediaTypeOrNull()) }
            params.headline?.let { parts["headline"] = it.toRequestBody("text/plain".toMediaTypeOrNull()) }
            params.mentorReason?.let { parts["mentorReason"] = it.toRequestBody("text/plain".toMediaTypeOrNull()) }
            params.greatestAchievement?.let { parts["greatestAchievement"] = it.toRequestBody("text/plain".toMediaTypeOrNull()) }
            params.bio?.let { parts["bio"] = it.toRequestBody("text/plain".toMediaTypeOrNull()) }
            params.introVideo?.let { parts["introVideo"] = it.toRequestBody("text/plain".toMediaTypeOrNull()) }

            val avatarPart: MultipartBody.Part? = params.avatarPath?.let { path ->
                val file = File(path)
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
                AppResult.failure("HTTP ${resp.code()}: ${resp.errorBody()?.string() ?: resp.message()}")
            }
        }
    }
