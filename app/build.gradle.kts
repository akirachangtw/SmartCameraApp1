plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.akirachang.ai.photographer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.akirachang.ai.photographer"
        minSdk = 24
        targetSdk = 36
        versionCode = 34
        versionName = "2.4.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true

            // 模擬器專用構建標記
            buildConfigField("Boolean", "EMULATOR_MODE", "true")
            buildConfigField("Boolean", "USE_TEST_IMAGES", "true")
            buildConfigField("Boolean", "ENABLE_PERFORMANCE_LOGGING", "false")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField("Boolean", "EMULATOR_MODE", "false")
            buildConfigField("Boolean", "USE_TEST_IMAGES", "false")
            buildConfigField("Boolean", "ENABLE_PERFORMANCE_LOGGING", "true")
        }
    }

    // 產品風味：區分模擬器和實機
    flavorDimensions += "target"
    productFlavors {
        create("emulator") {
            dimension = "target"
            applicationIdSuffix = ".emulator"
            versionNameSuffix = "-emulator"

            buildConfigField("Boolean", "IS_EMULATOR_BUILD", "true")
            buildConfigField("Boolean", "MOCK_CAMERA_DATA", "true")
        }

        create("device") {
            dimension = "target"
            buildConfigField("Boolean", "IS_EMULATOR_BUILD", "false")
            buildConfigField("Boolean", "MOCK_CAMERA_DATA", "false")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    // === AndroidX 核心 ===
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // === Compose UI ===
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // === CameraX ===
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)
    implementation(libs.androidx.camera.mlkit.vision)

    // === Hilt 依賴注入 ===
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // === 協程 ===
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.play.services)

    // === 權限處理 ===
    implementation(libs.accompanist.permissions)

    // === 圖像處理 ===
    implementation(libs.coil.compose)

    // === 資料持久化 ===
    implementation(libs.androidx.datastore.preferences)

    // === Logging ===
    implementation("com.jakewharton.timber:timber:5.0.1")

    // === ML Kit ===
    implementation(libs.mlkit.objectDetection)
    implementation(libs.mlkit.faceDetection)

    // === 測試 ===
    testImplementation(libs.junit)
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.google.truth:truth:1.1.5")

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}