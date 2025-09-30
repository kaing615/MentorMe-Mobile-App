package com.mentorme.app.core.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

fun copyUriToCache(context: Context, uri: Uri, fileName: String = "avatar_upload.jpg"): String? {
    return try {
        val input = context.contentResolver.openInputStream(uri) ?: run {
            android.util.Log.e("copyUriToCache", "❌ Không thể mở InputStream từ Uri: $uri")
            return null
        }
        val outFile = File(context.cacheDir, fileName)
        FileOutputStream(outFile).use { output -> input.copyTo(output) }
        input.close()
        android.util.Log.d("copyUriToCache", "✅ File được copy vào cache: ${outFile.absolutePath}")
        outFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
