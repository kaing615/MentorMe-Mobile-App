package com.mentorme.app.data.repository.impl

import android.util.Log
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.auth.AuthResponse
import com.mentorme.app.data.dto.auth.SignInRequest
import com.mentorme.app.data.dto.auth.SignUpRequest
import com.mentorme.app.data.dto.auth.VerifyOtpRequest
import com.mentorme.app.data.network.api.auth.AuthApiService
import com.mentorme.app.data.repository.AuthRepository
import com.mentorme.app.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService
) : AuthRepository {

    private val TAG = "AuthRepositoryImpl"

    override suspend fun signUp(request: SignUpRequest): AppResult<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔥🔥🔥 SIGNUP API CALL - EMAIL: ${request.email} 🔥🔥🔥")
                val resp = authApiService.signUp(request)
                if (resp.isSuccessful) {
                    resp.body()?.let {
                        Log.d(TAG, "SignUp success: ${it.message}")
                        AppResult.success(it)
                    } ?: AppResult.failure(IllegalStateException("Response body is null"))
                } else {
                    Log.e(TAG, "SignUp failed: HTTP ${resp.code()}")
                    AppResult.failure(Exception("HTTP ${resp.code()}: ${resp.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "SignUp exception: ${e.message}", e)
                AppResult.failure(e)
            }
        }

    override suspend fun signUpMentor(request: SignUpRequest): AppResult<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔥🔥🔥 SIGNUP MENTOR API CALL - EMAIL: ${request.email} 🔥🔥🔥")
                val resp = authApiService.signUpMentor(request)
                if (resp.isSuccessful) {
                    resp.body()?.let {
                        Log.d(TAG, "SignUpMentor success: ${it.message}")
                        AppResult.success(it)
                    } ?: AppResult.failure(IllegalStateException("Response body is null"))
                } else {
                    Log.e(TAG, "SignUpMentor failed: HTTP ${resp.code()}")
                    AppResult.failure(Exception("HTTP ${resp.code()}: ${resp.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "SignUpMentor exception: ${e.message}", e)
                AppResult.failure(e)
            }
        }

    override suspend fun signIn(request: SignInRequest): AppResult<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔥🔥🔥 SIGNIN API CALL - EMAIL: ${request.email} 🔥🔥🔥")
                Log.d(TAG, "=== STARTING SIGNIN API CALL ===")
                val resp = authApiService.signIn(request)
                Log.d(TAG, "API call completed! Response code: ${resp.code()}")

                if (resp.isSuccessful) {
                    resp.body()?.let {
                        Log.d(TAG, "SignIn success: ${it.message}")
                        AppResult.success(it)
                    } ?: AppResult.failure(IllegalStateException("Response body is null"))
                } else {
                    Log.e(TAG, "SignIn failed: HTTP ${resp.code()}")
                    AppResult.failure(Exception("HTTP ${resp.code()}: ${resp.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "=== SIGNIN API EXCEPTION ===")
                Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
                Log.e(TAG, "Exception message: ${e.message}", e)
                Log.e(TAG, "This should happen if backend is not running!")
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

    // Missing abstract methods implementation
    override suspend fun login(email: String, password: String): AppResult<com.mentorme.app.data.dto.AuthResponse> {
        // Convert to signIn call
        val result = signIn(SignInRequest(email, password))
        return when (result) {
            is AppResult.Success -> AppResult.success(result.data as com.mentorme.app.data.dto.AuthResponse)
            is AppResult.Error -> AppResult.failure(result.throwable)
            AppResult.Loading -> AppResult.loading()
        }
    }

    override suspend fun register(email: String, password: String, name: String, role: String): AppResult<com.mentorme.app.data.dto.AuthResponse> {
        // Convert to signUp call
        val result = signUp(SignUpRequest(username = name, email = email, password = password, confirmPassword = password, displayName = name))
        return when (result) {
            is AppResult.Success -> AppResult.success(result.data as com.mentorme.app.data.dto.AuthResponse)
            is AppResult.Error -> AppResult.failure(result.throwable)
            AppResult.Loading -> AppResult.loading()
        }
    }

    override suspend fun logout(): AppResult<Unit> {
        return AppResult.success(Unit)
    }

    override suspend fun getCurrentUser(): AppResult<User> {
        return AppResult.failure(Exception("Not implemented yet"))
    }

    override fun getToken(): Flow<String?> {
        return flowOf(null) // Mock implementation
    }

    override suspend fun saveToken(token: String) {
        // Mock implementation - you can implement with SharedPreferences later
        Log.d(TAG, "Saving token: $token")
    }

    override suspend fun clearToken() {
        // Mock implementation - you can implement with SharedPreferences later
        Log.d(TAG, "Clearing token")
    }
}
