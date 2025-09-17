package com.mentorme.app.data.repository.impl

import com.mentorme.app.core.datastore.DataStoreManager
import com.mentorme.app.core.utils.Result
import com.mentorme.app.data.dto.AuthResponse
import com.mentorme.app.data.dto.LoginRequest
import com.mentorme.app.data.dto.RegisterRequest
import com.mentorme.app.data.model.User
import com.mentorme.app.data.remote.MentorMeApi
import com.mentorme.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: MentorMeApi,
    private val dataStoreManager: DataStoreManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                dataStoreManager.saveToken(authResponse.token)
                dataStoreManager.saveUserInfo(
                    userId = authResponse.user.id,
                    email = authResponse.user.email,
                    name = authResponse.user.fullName,
                    role = authResponse.user.role.name
                )
                Result.Success(authResponse)
            } else {
                Result.Error(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun register(email: String, password: String, name: String, role: String): Result<AuthResponse> {
        return try {
            val response = api.register(RegisterRequest(email, password, name, role))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                dataStoreManager.saveToken(authResponse.token)
                dataStoreManager.saveUserInfo(
                    userId = authResponse.user.id,
                    email = authResponse.user.email,
                    name = authResponse.user.fullName,
                    role = authResponse.user.role.name
                )
                Result.Success(authResponse)
            } else {
                Result.Error(Exception("Registration failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            api.logout()
            dataStoreManager.clearToken()
            dataStoreManager.clearUserInfo()
            Result.Success(Unit)
        } catch (e: Exception) {
            // Still clear local data even if API call fails
            dataStoreManager.clearToken()
            dataStoreManager.clearUserInfo()
            Result.Success(Unit)
        }
    }

    override suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = api.getCurrentUser()
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error(Exception("Failed to get current user: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun getToken(): Flow<String?> {
        return dataStoreManager.getToken()
    }

    override suspend fun saveToken(token: String) {
        dataStoreManager.saveToken(token)
    }

    override suspend fun clearToken() {
        dataStoreManager.clearToken()
    }
}
