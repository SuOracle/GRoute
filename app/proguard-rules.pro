-keep class gozarcore.** { *; }
-keep class go.** { *; }
-keepclassmembers class gozarcore.** { *; }
-dontwarn gozarcore.**
-dontwarn go.**

-keep class org.strongswan.android.** { *; }
-keep interface org.strongswan.android.** { *; }
-keepclassmembers class org.strongswan.android.** { *; }
-dontwarn org.strongswan.android.**

-keep class net.gozar.app.GozarApplication { *; }
-keep class net.gozar.app.GozarVpnService { *; }
-keep class net.gozar.app.QsTileService { *; }

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers class net.gozar.app.** {
    <fields>;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault

-keepclassmembers class * extends android.app.Application {
    <init>();
}

-keepclassmembers class * extends android.app.Service {
    <init>();
}

-keep class com.jcraft.jsch.** { *; }
-dontwarn com.jcraft.jsch.**
-dontwarn org.slf4j.**
-dontwarn org.bouncycastle.**
-dontwarn org.apache.**