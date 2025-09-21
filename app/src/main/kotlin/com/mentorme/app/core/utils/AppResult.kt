// app/src/main/java/com/mentorme/app/core/utils/AppResult.kt
package com.mentorme.app.core.utils

sealed interface AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>
    data class Error(val throwable: Throwable) : AppResult<Nothing>
    data object Loading : AppResult<Nothing>

    companion object {
        fun <T> success(data: T): AppResult<T> = Success(data)
        fun failure(throwable: Throwable): AppResult<Nothing> = Error(throwable)
        fun loading(): AppResult<Nothing> = Loading
    }
}

inline fun <T> AppResult<T>.onSuccess(block: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) block(data); return this
}
inline fun <T> AppResult<T>.onError(block: (Throwable) -> Unit): AppResult<T> {
    if (this is AppResult.Error) block(throwable); return this
}
inline fun <R, T> AppResult<T>.fold(
    onSuccess: (T) -> R,
    onError: (Throwable) -> R,
    onLoading: () -> R
): R = when (this) {
    is AppResult.Success -> onSuccess(data)
    is AppResult.Error -> onError(throwable)
    AppResult.Loading -> onLoading()
}
