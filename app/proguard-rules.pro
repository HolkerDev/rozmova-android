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
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
  *;
}

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

# Credentials Manager
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
  *;
}

# Ktor rules - prevent class loading issues
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.atomicfu.**
-dontwarn io.netty.**
-dontwarn com.typesafe.**
-dontwarn org.slf4j.**

# Ktor plugins - specifically preserve HttpTimeout and related classes
-keep class io.ktor.client.plugins.** { *; }
-keep class io.ktor.client.engine.** { *; }
-keep class io.ktor.client.features.** { *; }
-keep class io.ktor.client.plugins.HttpTimeout { *; }
-keep class io.ktor.client.plugins.HttpTimeoutConfig { *; }
-keep class io.ktor.client.plugins.HttpTimeoutException { *; }

# WebSocket specific
-keep class io.ktor.client.plugins.websocket.** { *; }
-keep class io.ktor.websocket.** { *; }

# OkHttp engine
-keep class io.ktor.client.engine.okhttp.** { *; }
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-dontwarn io.ktor.client.engine.okhttp.**

# Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-dontnote kotlinx.serialization.SerializationKt

-keep,includedescriptorclasses class **$$serializer { *; }
-keepclassmembers class ** {
    *** Companion;
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Ensure no obfuscation of plugin classes
-keepnames class io.ktor.client.plugins.*
