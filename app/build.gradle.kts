plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.jarimanis.jarimanis"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.jarimanis.jarimanis"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // 1. RETROFIT & NETWORKING (Untuk komunikasi dengan server VPS/Node.js)
    implementation(libs.retrofit)
    implementation(libs.converter.gson) // Konversi JSON ke Data Class
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor) // Untuk melihat log API di Logcat

    // 2. LIFECYCLE & COROUTINES (Untuk MVVM dan proses asinkron)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose) // Untuk collectAsStateWithLifecycle
    implementation(libs.kotlinx.coroutines.android)

    // 3. COMPOSE NAVIGATION (Untuk Navigasi Dinamis Siswa, Guru, Dosen)
    implementation(libs.androidx.navigation.compose)

    // 4. DATASTORE PREFERENCES (Untuk menyimpan Token JWT sesi login secara aman)
    implementation(libs.androidx.datastore.preferences)

    // 5. COIL (Untuk memuat & menampilkan gambar/foto aksi nyata dari URL)
    implementation(libs.coil.compose)

    // 6. COMPOSE MATERIAL ICONS (Untuk icon di Bottom Navigation)
    implementation(libs.androidx.compose.material.icons.extended)

    // YouTube Player by Pierfrancesco Soffritti
    implementation(libs.core)

    implementation(libs.androidx.material3)
}