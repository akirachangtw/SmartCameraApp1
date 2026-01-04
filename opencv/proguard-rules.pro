# OpenCV ProGuard Rules
# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep OpenCV classes
-keep class org.opencv.** { *; }
