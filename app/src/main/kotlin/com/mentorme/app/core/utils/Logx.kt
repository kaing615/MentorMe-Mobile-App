package com.mentorme.app.core.utils

/**
 * Lightweight debug logger facade to centralize BuildConfig checks and Android Log calls.
 * Use for development diagnostics only; avoids leaking sensitive info in production.
 */
object Logx {
    // Reflection-based DEBUG flag to avoid direct dependency on generated BuildConfig
    private val debugFlag: Boolean by lazy {
        runCatching {
            Class.forName("com.mentorme.app.BuildConfig")
                .getField("DEBUG")
                .getBoolean(null)
        }.getOrElse { false }
    }
    val isDebug: Boolean get() = debugFlag

    inline fun d(tag: String, message: () -> String) {
        if (isDebug) android.util.Log.d(tag, message())
    }

    inline fun e(tag: String, message: () -> String, t: Throwable? = null) {
        if (isDebug) android.util.Log.e(tag, message(), t)
    }
}
