// OpenCV 4.13.0 Android Library Module
// Converted to Kotlin DSL for SmartCameraApp1

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
}

val openCVersionName = "4.13.0"
val openCVersionCode = ((4 * 100 + 13) * 100 + 0) * 10 + 0

println("OpenCV: $openCVersionName")

android {
    namespace = "org.opencv"
    compileSdk = 36  // Match host app compileSdk
    ndkVersion = "29.0.14206865"  // Use newer NDK version

    defaultConfig {
        minSdk = 21
        // Note: OpenCV supports API 21+, but host app uses minSdk 24

        versionCode = openCVersionCode
        versionName = openCVersionName

        externalNativeBuild {
            cmake {
                arguments.add("-DANDROID_STL=c++_shared")
                targets.add("opencv_jni_shared")
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildTypes {
        debug {
            packaging {
                jniLibs {
                    // Controlled by OpenCV CMake scripts
                    keepDebugSymbols.add("**/*.so")
                }
            }
        }
        release {
            packaging {
                jniLibs {
                    // Controlled by OpenCV CMake scripts
                    keepDebugSymbols.add("**/*.so")
                }
            }
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("native/libs")
            java.srcDirs("java/src")
            res.srcDirs("java/res")
            manifest.srcFile("java/AndroidManifest.xml")
        }
    }

    externalNativeBuild {
        cmake {
            path = file("libcxx_helper/CMakeLists.txt")
        }
    }

    buildFeatures {
        prefabPublishing = true
    }

    prefab {
        create("opencv_jni_shared") {
            headers = "native/jni/include"
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "org.opencv"
            artifactId = "opencv"
            version = "4.13.0"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

dependencies {
    // No external dependencies
}
