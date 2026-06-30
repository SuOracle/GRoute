# Keep the gomobile bridge and Go runtime classes (accessed via JNI/reflection)
-keep class gozarcore.** { *; }
-keep class go.** { *; }

# Keep Xray/Go-generated classes that may be referenced by name
-keepclassmembers class gozarcore.** { *; }

# Gson/JSON config models are built by reflection — keep their fields
-keepclassmembers class net.gozar.app.** {
    <fields>;
}

# Suppress warnings for the Go-generated code
-dontwarn gozarcore.**
-dontwarn go.**