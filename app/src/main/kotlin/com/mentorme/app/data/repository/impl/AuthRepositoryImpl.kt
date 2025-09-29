package com.mentorme.app.data.repository.impl

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.auth.AuthResponse
import com.mentorme.app.data.dto.auth.SignInRequest
import com.mentorme.app.data.dto.auth.SignUpRequest
import com.mentorme.app.data.dto.auth.VerifyOtpRequest
import com.mentorme.app.data.dto.auth.ResendOtpRequest
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
                    resp.body()?.let { authResponse ->
                        if (authResponse.success) {
                            AppResult.success(authResponse)
                        } else {
                            // Server returned an error in the response body
                            AppResult.failure(authResponse.message)
                        }
                    } ?: AppResult.failure("Response body is null")
                } else {
                    // Parse error response body if available
                    val errorBody = resp.errorBody()?.string()
                    AppResult.failure("HTTP ${resp.code()}: ${errorBody ?: resp.message()}")
                }
            } catch (e: Exception) {
                AppResult.failure(e.message ?: "Unknown error occurred")
            }
        }

    override suspend fun signUpMentor(request: SignUpRequest): AppResult<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val resp = authApiService.signUpMentor(request)
                if (resp.isSuccessful) {
                    resp.body()?.let { authResponse ->
                        if (authResponse.success) {
                            AppResult.success(authResponse)
                        } else {
                            // Server returned an error in the response body
                            AppResult.failure(authResponse.message)
                        }
                    } ?: AppResult.failure("Response body is null")
                } else {
                    // Parse error response body if available
                    val errorBody = resp.errorBody()?.string()
                    AppResult.failure("HTTP ${resp.code()}: ${errorBody ?: resp.message()}")
                }
            } catch (e: Exception) {
                AppResult.failure(e.message ?: "Unknown error occurred")
            }
        }

    override suspend fun signIn(request: SignInRequest): AppResult<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val resp = authApiService.signIn(request)
                if (resp.isSuccessful) {
                    resp.body()?.let { authResponse ->
                        if (authResponse.success) {
                            AppResult.success(authResponse)
                        } else {
                            // Server returned an error in the response body
                            AppResult.failure(authResponse.message)
                        }
                    } ?: AppResult.failure("Response body is null")
                } else {
                    // Parse error response body if available
                    val errorBody = resp.errorBody()?.string()
                    AppResult.failure("HTTP ${resp.code()}: ${errorBody ?: resp.message()}")
                }
            } catch (e: Exception) {
                AppResult.failure(e.message ?: "Unknown error occurred")
            }
        }

    override suspend fun verifyOtp(request: VerifyOtpRequest): AppResult<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val resp = authApiService.verifyOtp(request)
                if (resp.isSuccessful) {
                    resp.body()?.let { authResponse ->
                        if (authResponse.success) {
                            AppResult.success(authResponse)
                        } else {
                            // Server returned an error in the response body
                            AppResult.failure(authResponse.message)
                        }
                    } ?: AppResult.failure("Response body is null")
                } else {
                    // Parse error response body if available
                    val errorBody = resp.errorBody()?.string()
                    AppResult.failure("HTTP ${resp.code()}: ${errorBody ?: resp.message()}")
                }
            } catch (e: Exception) {
                AppResult.failure(e.message ?: "Unknown error occurred")
            }
        }

    override suspend fun resendOtp(request: ResendOtpRequest): AppResult<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                // Tạo SignUpRequest với thông tin từ email - backend sẽ nhận ra user đã tồn tại và gửi lại OTP
                val signUpRequest = SignUpRequest(
                    userName = request.email.substringBefore("@"), // Tạo username đơn giản từ email
                    email = request.email,
                    password = "temp_password_for_resend" // Password tạm cho resend
                )
                val resp = authApiService.resendOtp(signUpRequest)
                if (resp.isSuccessful) {
                    resp.body()?.let { authResponse ->
                        if (authResponse.success) {
                            AppResult.success(authResponse)
                        } else {
                            // Server returned an error in the response body
                            AppResult.failure(authResponse.message)
                        }
                    } ?: AppResult.failure("Response body is null")
                } else {
                    // Parse error response body if available
                    val errorBody = resp.errorBody()?.string()
                    AppResult.failure("HTTP ${resp.code()}: ${errorBody ?: resp.message()}")
                }
            } catch (e: Exception) {
                AppResult.failure(e.message ?: "Unknown error occurred")
            }
        }

    override suspend fun signOut(): AppResult<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val resp = authApiService.signOut()
                if (resp.isSuccessful) {
                    resp.body()?.let { authResponse ->
                        if (authResponse.success) {
                            AppResult.success(authResponse)
                        } else {
                            // Server returned an error in the response body
                            AppResult.failure(authResponse.message)
                        }
                    } ?: AppResult.failure("Response body is null")
                } else {
                    // Parse error response body if available
                    val errorBody = resp.errorBody()?.string()
                    AppResult.failure("HTTP ${resp.code()}: ${errorBody ?: resp.message()}")
                }
            } catch (e: Exception) {
                AppResult.failure(e.message ?: "Unknown error occurred")
            }
        }

}
