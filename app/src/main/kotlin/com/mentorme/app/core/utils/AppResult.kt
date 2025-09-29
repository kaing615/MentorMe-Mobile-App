// app/src/main/java/com/mentorme/app/core/utils/AppResult.kt
package com.mentorme.app.core.utils

sealed interface AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>
    data class Error(val throwable: String) : AppResult<Nothing>
    data object Loading : AppResult<Nothing>

    companion object {
        fun <T> success(data: T): AppResult<T> = Success(data)
        fun failure(throwable: Throwable): AppResult<Nothing> = Error(throwable.message ?: "Unknown error")
        fun failure(message: String): AppResult<Nothing> = Error(message)
        fun loading(): AppResult<Nothing> = Loading
    }
}
