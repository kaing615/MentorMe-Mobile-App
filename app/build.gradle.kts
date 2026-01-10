plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
}


configurations.all {
    resolutionStrategy {
        // Force activity libraries to compatible versions
        force("androidx.activity:activity:1.9.2")
        force("androidx.activity:activity-compose:1.9.2")
        force("androidx.activity:activity-ktx:1.9.2")

        // Force Kotlin stdlib to match Kotlin compiler version 2.0.20
        force("org.jetbrains.kotlin:kotlin-stdlib:2.0.20")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.0.20")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.0.20")
        force("org.jetbrains.kotlin:kotlin-reflect:2.0.20")
    }
}

android {
    namespace = "com.mentorme.app"
    compileSdk = 35   // <-- nên dùng stable, không phải 36 preview

    defaultConfig {
        applicationId = "com.mentorme.app"
        minSdk = 26
        targetSdk = 35   // match compileSdk stable
        versionCode = 1
        versionName = "1.0"

        val stunUrl = (project.findProperty("WEBRTC_STUN_URL") as String?)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: "stun:stun.l.google.com:19302"
        val turnUrl = (project.findProperty("WEBRTC_TURN_URL") as String?)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: ""
        val turnUser = (project.findProperty("WEBRTC_TURN_USERNAME") as String?)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: ""
        val turnPass = (project.findProperty("WEBRTC_TURN_PASSWORD") as String?)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: ""

        fun escape(value: String): String = value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")

        buildConfigField("String", "WEBRTC_STUN_URL", "\"${escape(stunUrl)}\"")
        buildConfigField("String", "WEBRTC_TURN_URL", "\"${escape(turnUrl)}\"")
        buildConfigField("String", "WEBRTC_TURN_USERNAME", "\"${escape(turnUser)}\"")
        buildConfigField("String", "WEBRTC_TURN_PASSWORD", "\"${escape(turnPass)}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            pickFirsts.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")

            excludes.addAll(
                listOf(
                    "META-INF/DEPENDENCIES",
                    "META-INF/LICENSE",
                    "META-INF/LICENSE.txt",
                    "META-INF/NOTICE",
                    "META-INF/NOTICE.txt"
                )
            )
        }
    }

    packaging {
        resources {
            pickFirsts.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")

            excludes.addAll(
                listOf(
                    "META-INF/DEPENDENCIES",
                    "META-INF/LICENSE",
                    "META-INF/LICENSE.txt",
                    "META-INF/NOTICE",
                    "META-INF/NOTICE.txt"
                )
            )
        }
    }
}

dependencies {
    // Core Android libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Firebase BOM - manages all Firebase versions
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.messaging.ktx)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Compose BOM - manages all Compose versions (removed duplicates)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-text")
    implementation("androidx.compose.foundation:foundation-layout")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.runtime:runtime-saveable")

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt for dependency injection
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.material3)
    implementation(libs.identity.jvm)
    implementation(libs.litertlm.jvm)
    implementation(libs.androidx.foundation.layout)
    ksp(libs.hilt.compiler)

    // Room database (removed duplicate)
    implementation("androidx.room:room-ktx:2.6.1")

    // Haze for blur effects
    implementation(libs.haze)

    ksp(libs.hilt.compiler)

    // Room database (removed duplicate)
    implementation("androidx.room:room-ktx:2.6.1")

    // Haze for blur effects
    implementation(libs.haze)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Data storage
    implementation(libs.androidx.datastore.preferences)

    // Image loading (removed duplicate coil)
    implementation(libs.coil.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // JSON
    implementation(libs.gson)

    // Validation
    implementation("io.konform:konform-jvm:0.4.0")

    // Socket.IO
    implementation("io.socket:socket.io-client:2.1.0") {
        exclude(group = "org.json", module = "json")
    }

    // WebRTC (Maven Central)
    implementation("org.jitsi:webrtc:124.0.0")
    
    // ML Kit Selfie Segmentation for background blur
    implementation("com.google.mlkit:segmentation-selfie:16.0.0-beta6")

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
