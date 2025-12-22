plugins {
    id("com.android.application") version "8.6.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20" apply false
    id("org.jetbrains.kotlin.kapt") version "2.0.20" apply false

    // Hilt plugin: PHẢI có version ở root
    id("com.google.dagger.hilt.android") version "2.51.1" apply false

    // (tuỳ chọn) Google Services nếu dùng Firebase features yêu cầu
    id("com.google.gms.google-services") version "4.4.4" apply false
}
