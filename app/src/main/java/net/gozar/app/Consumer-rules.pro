-keep class org.strongswan.android.logic.CharonVpnService { *; }
-keep class org.strongswan.android.logic.CharonVpnService$* { *; }
-keep class org.strongswan.android.logic.VpnStateService { *; }
-keep class org.strongswan.android.logic.VpnStateService$* { *; }
-keep class org.strongswan.android.logic.NetworkManager { *; }
-keep class org.strongswan.android.logic.imc.** { *; }
-keep class org.strongswan.android.data.VpnProfile { *; }
-keep class org.strongswan.android.data.VpnType { *; }

-keepclasseswithmembernames class * {
    native <methods>;
}