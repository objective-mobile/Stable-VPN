# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Exclude :vpn:data module from obfuscation
# Keep all classes in OpenVPN packages
-keep class de.blinkt.openvpn.** { *; }
-keep class org.spongycastle.** { *; }

# Keep all native methods (JNI)
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep VPN-related AIDL interfaces
-keep class * implements android.os.IInterface { *; }

# Preserve VPN service classes and their methods
-keep class * extends android.net.VpnService { *; }
-keep class * extends android.app.Service { *; }