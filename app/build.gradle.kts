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

    // v2.9.6 Phase 2.1: NDK version for 16 KB page size alignment (Android 15+)
    ndkVersion = "27.0.12077973"

    defaultConfig {
        applicationId = "com.akirachang.ai.photographer"
        minSdk = 24
        targetSdk = 36
        versionCode = 85
        versionName = "2.12.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true

            // v2.6.1: Let flavor decide emulator mode, not buildType
            buildConfigField("Boolean", "EMULATOR_MODE", "false")
            buildConfigField("Boolean", "USE_TEST_IMAGES", "false")
            buildConfigField("Boolean", "ENABLE_PERFORMANCE_LOGGING", "false")

            // v2.8.4 Plan B: Fused YUV+Transform RenderScript kernel (default: true for testing)
            buildConfigField("Boolean", "ENABLE_FUSED_TRANSFORM", "true")

            // v2.8.6 Phase 2: PreviewView migration for Graphics memory fix
            buildConfigField("Boolean", "USE_PREVIEWVIEW_DISPLAY", "true")
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

            // v2.8.4 Plan B: Fused YUV+Transform RenderScript kernel (disabled in release)
            buildConfigField("Boolean", "ENABLE_FUSED_TRANSFORM", "false")

            buildConfigField("Boolean", "USE_PREVIEWVIEW_DISPLAY", "true")
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
            // v2.x.x Phase 6: Set to false to use virtual 3D camera on emulator for optical flow testing
            buildConfigField("Boolean", "MOCK_CAMERA_DATA", "false")
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
        // v2.9.6 Phase 2.4: Removed useLegacyPackaging, using pageSizeCompat instead
        // Let AGP 8.10.1 handle 16KB ZIP alignment automatically
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
    // Cueshot D0: SplashScreen API — holds splash until ML Kit pre-warm completes
    implementation("androidx.core:core-splashscreen:1.0.1")

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

    // === EXIF (v2.9.6 Phase 1) ===
    implementation(libs.androidx.exifinterface)

    // === Location Services (v2.9.7 Day 1-2: GPS EXIF) ===
    implementation(libs.play.services.location)

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

// ==========================================
// Test Image Generation Automation (v2.7.0)
// ==========================================

/**
 * Configuration for automated test image generation
 * Optimized for 8GB RAM systems
 */
object TestImageConfig {
    const val PYTHON_CMD = "python"
    const val SCRIPT_PATH = "tools/generate_assets.py"
    const val DEFAULT_PROMPTS = "tools/prompts_v2.6.0_hdr_test.txt"
    const val OUTPUT_DIR = "app/src/debug/assets/test_images"
    const val METADATA_FILE = "image-generation-metadata.json"
}

/**
 * Check if Python is available in the system
 */
fun isPythonAvailable(): Boolean {
    return try {
        val process = ProcessBuilder("python", "--version")
            .redirectErrorStream(true)
            .start()
        process.waitFor()
        process.exitValue() == 0
    } catch (e: Exception) {
        false
    }
}

/**
 * Verify Python environment for test image generation
 */
tasks.register<Exec>("checkPythonEnv") {
    group = "verification"
    description = "Verify Python and dependencies for test image generation"

    commandLine(TestImageConfig.PYTHON_CMD, "--version")

    isIgnoreExitValue = true

    doLast {
        if (executionResult.get().exitValue != 0) {
            logger.warn("⚠️  Python not found - test image generation disabled")
            logger.warn("   Install Python 3.x to enable automated image generation")
            logger.warn("   Download: https://www.python.org/downloads/")
        } else {
            logger.lifecycle("✅ Python environment OK")
        }
    }
}

/**
 * Scan prompt files and check which images need generation
 */
tasks.register("scanTestImagePrompts") {
    group = "test images"
    description = "Scan prompt files and check which images need generation"

    dependsOn("checkPythonEnv")

    // Configuration cache compatible: resolve files and properties at configuration time
    val promptFilePath = rootProject.layout.projectDirectory.file(TestImageConfig.DEFAULT_PROMPTS)
    val outputDirPath = rootProject.layout.projectDirectory.dir(TestImageConfig.OUTPUT_DIR)
    val skipIfExists = providers.gradleProperty("skipImageGen").map { it.toBoolean() }.getOrElse(false)

    doLast {
        val promptFile = promptFilePath.asFile
        val outputDir = outputDirPath.asFile

        if (!promptFile.exists()) {
            logger.warn("⚠️  Prompt file not found: ${promptFile.path}")
            logger.warn("   Test image generation skipped")
            return@doLast
        }

        outputDir.mkdirs()

        // Parse prompt file to count expected images
        val expectedImages = promptFile.readLines()
            .filter { line ->
                val trimmed = line.trim()
                trimmed.isNotEmpty() && !trimmed.startsWith("#")
            }
            .size

        val existingImages = outputDir.listFiles()
            ?.filter { it.extension.lowercase() in listOf("jpg", "png") }
            ?.size ?: 0

        logger.lifecycle("📊 Test Image Status:")
        logger.lifecycle("   Expected: $expectedImages images")
        logger.lifecycle("   Existing: $existingImages images")
        logger.lifecycle("   Missing:  ${expectedImages - existingImages} images")

        if (existingImages >= expectedImages && !skipIfExists) {
            logger.lifecycle("✅ All test images present - generation will be skipped")
        } else if (existingImages < expectedImages) {
            logger.lifecycle("🔄 Image generation needed on next build")
        }
    }
}

/**
 * Generate missing test images using Gemini API
 * Respects rate limits and free tier constraints
 */
tasks.register<Exec>("generateTestImages") {
    group = "test images"
    description = "Generate missing test images using Gemini API"

    dependsOn("checkPythonEnv", "scanTestImagePrompts")

    // Disable configuration cache for this task (involves external Python execution)
    notCompatibleWithConfigurationCache("Executes external Python script with dynamic file checking")

    // Configuration cache compatible: resolve files and properties at configuration time
    val outputDirPath = rootProject.layout.projectDirectory.dir(TestImageConfig.OUTPUT_DIR)
    val promptFilePath = rootProject.layout.projectDirectory.file(TestImageConfig.DEFAULT_PROMPTS)
    val skipImageGen = providers.gradleProperty("skipImageGen").map { it.toBoolean() }.getOrElse(false)
    val forceRegen = providers.gradleProperty("forceRegen").map { it.toBoolean() }.getOrElse(false)

    // Skip if explicitly disabled
    onlyIf {
        !skipImageGen
    }

    // Check if images already exist (incremental generation)
    onlyIf {
        if (forceRegen) {
            logger.lifecycle("🔄 Force regeneration enabled")
            true
        } else {
            val outputDir = outputDirPath.asFile
            val promptFile = promptFilePath.asFile

            if (!promptFile.exists()) {
                logger.warn("⚠️  Prompt file not found - skipping generation")
                false
            } else {
                val expectedCount = promptFile.readLines()
                    .filter { it.trim().isNotEmpty() && !it.trim().startsWith("#") }
                    .size

                val existingCount = outputDir.listFiles()
                    ?.filter { it.extension.lowercase() in listOf("jpg", "png") }
                    ?.size ?: 0

                if (existingCount >= expectedCount) {
                    logger.lifecycle("✅ Test images exist ($existingCount/$expectedCount) - skipping generation")
                    logger.lifecycle("   Use -PforceRegen=true to override")
                    false
                } else {
                    logger.lifecycle("📸 Generating missing images (${expectedCount - existingCount} needed)")
                    true
                }
            }
        }
    }

    // Verify API key present
    onlyIf {
        val apiKey = System.getenv("GEMINI_API_KEY")
        if (apiKey.isNullOrBlank()) {
            logger.warn("⚠️  GEMINI_API_KEY not set - test image generation skipped")
            logger.warn("   Set via: \$env:GEMINI_API_KEY='your_key' (Windows PowerShell)")
            logger.warn("   Set via: set GEMINI_API_KEY=your_key (Windows CMD)")
            logger.warn("   Set via: export GEMINI_API_KEY='your_key' (Linux/Mac)")
            false
        } else {
            true
        }
    }

    // Verify Python available
    onlyIf {
        if (!isPythonAvailable()) {
            logger.warn("⚠️  Python not available - test image generation skipped")
            false
        } else {
            true
        }
    }

    commandLine(
        TestImageConfig.PYTHON_CMD,
        TestImageConfig.SCRIPT_PATH,
        TestImageConfig.DEFAULT_PROMPTS
    )

    // Graceful failure - don't break build
    isIgnoreExitValue = true

    doFirst {
        logger.lifecycle("🖼️  Starting test image generation...")
        logger.lifecycle("   This may take several minutes (API rate limits)")
    }

    doLast {
        val exitValue = executionResult.get().exitValue
        when (exitValue) {
            0 -> {
                logger.lifecycle("✅ Test images generated successfully")

                // Verify generated images
                val outputDir = outputDirPath.asFile
                val imageCount = outputDir.listFiles()
                    ?.filter { it.extension.lowercase() in listOf("jpg", "png") }
                    ?.size ?: 0
                logger.lifecycle("   Total images: $imageCount")
            }
            1 -> {
                logger.warn("⚠️  Some images failed to generate - check logs")
                logger.warn("   Build continues with existing images")
            }
            else -> {
                logger.error("❌ Image generation failed (exit code: $exitValue)")
                logger.error("   Troubleshooting:")
                logger.error("   1. Verify API key: echo \$env:GEMINI_API_KEY")
                logger.error("   2. Install packages: pip install google-genai pillow")
                logger.error("   3. Check rate limits (free tier: ~2 images/min)")
                logger.error("   4. Skip generation: ./gradlew assembleEmulatorDebug -PskipImageGen=true")
                logger.warn("   Build continues with existing images")
            }
        }
    }
}

/**
 * Convenience task for manual image generation
 */
tasks.register("updateTestImages") {
    group = "test images"
    description = "Manually trigger test image generation (bypasses existence check)"

    doFirst {
        // Force regeneration by setting property
        project.extra["forceRegen"] = "true"
    }

    finalizedBy("generateTestImages")
}

/**
 * Pre-Build Hook: Automatically generate test images before emulatorDebug builds
 * Integrates seamlessly into the development workflow
 */
tasks.matching { it.name == "preEmulatorDebugBuild" }.configureEach {
    dependsOn("generateTestImages")
}
