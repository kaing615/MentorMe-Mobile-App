package com.mentorme.app.data.repository.impl

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.auth.AuthResponse
import com.mentorme.app.data.dto.auth.SignInRequest
import com.mentorme.app.data.dto.auth.SignUpRequest
import com.mentorme.app.data.dto.auth.VerifyOtpRequest
import com.mentorme.app.data.network.api.auth.AuthApiService
import com.mentorme.app.data.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService
) : AuthRepository {

    override suspend fun signUp(request: SignUpRequest): AppResult<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val resp = authApiService.signUp(request)
                if (resp.isSuccessful) {
                    resp.body()?.let {
                        AppResult.success(it)
                    } ?: AppResult.failure(IllegalStateException("Response body is null"))
                } else {
                    AppResult.failure(Exception("HTTP ${resp.code()}: ${resp.message()}"))
                }
            } catch (e: Exception) {
                AppResult.failure(e)
            }
        }

    override suspend fun signUpMentor(request: SignUpRequest): AppResult<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val resp = authApiService.signUpMentor(request)
                if (resp.isSuccessful) {
                    resp.body()?.let {
                        AppResult.success(it)
                    } ?: AppResult.failure(IllegalStateException("Response body is null"))
                } else {
                    AppResult.failure(Exception("HTTP ${resp.code()}: ${resp.message()}"))
                }
            } catch (e: Exception) {
                AppResult.failure(e)
            }
        }

    override suspend fun signIn(request: SignInRequest): AppResult<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val resp = authApiService.signIn(request)

                if (resp.isSuccessful) {
                    resp.body()?.let {
                        AppResult.success(it)
                    } ?: AppResult.failure(IllegalStateException("Response body is null"))
                } else {
                    AppResult.failure(Exception("HTTP ${resp.code()}: ${resp.message()}"))
                }
            } catch (e: Exception) {
                AppResult.failure(e)
            }
        }

    override suspend fun verifyOtp(request: VerifyOtpRequest): AppResult<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val resp = authApiService.verifyOtp(request)
                if (resp.isSuccessful) {
                    resp.body()?.let { AppResult.success(it) }
                        ?: AppResult.failure(IllegalStateException("Response body is null"))
                } else {
                    AppResult.failure(Exception("HTTP ${resp.code()}: ${resp.message()}"))
                }
            } catch (e: Exception) { AppResult.failure(e) }
        }

    override suspend fun signOut(): AppResult<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val resp = authApiService.signOut()
                if (resp.isSuccessful) {
                    resp.body()?.let { AppResult.success(it) }
                        ?: AppResult.failure(IllegalStateException("Response body is null"))
                } else {
                    AppResult.failure(Exception("HTTP ${resp.code()}: ${resp.message()}"))
                }
            } catch (e: Exception) { AppResult.failure(e) }
        }

}
