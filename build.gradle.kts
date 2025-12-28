plugins {
    id("com.android.application") version "8.6.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
    id("com.google.devtools.ksp") version "2.0.20-1.0.25" apply false

    // Hilt plugin: compatible with Kotlin 2.0.20 + KSP
    id("com.google.dagger.hilt.android") version "2.52" apply false

    // Google Services for Firebase
    id("com.google.gms.google-services") version "4.4.4" apply false
}
