-optimizationpasses 5
-allowaccessmodification
-repackageclasses ''
-overloadaggressively

-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}

-keepclassmembers class net.gozar.app.ConfigFile {
    private static final int[] P1;
    private static final int[] P2;
    private static final int[] P3;
    private static final int[] P4;
}

-keep class gozarcore.** { *; }
-keep class go.** { *; }

-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-dontwarn kotlinx.coroutines.**