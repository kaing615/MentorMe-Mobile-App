plugins {
    id("com.android.application") version "8.6.1" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false

    // Hilt plugin: compatible with Kotlin 2.1.0 + KSP
    id("com.google.dagger.hilt.android") version "2.53" apply false

    // Google Services for Firebase
    id("com.google.gms.google-services") version "4.4.4" apply false
}
