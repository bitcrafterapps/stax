# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# ── Debugging ────────────────────────────────────────────────────────────────
# Uncomment to preserve line numbers in release stack traces:
#-keepattributes SourceFile,LineNumberTable
#-renamesourcefileattribute SourceFile

# ── Kotlin ───────────────────────────────────────────────────────────────────
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep Kotlin metadata so reflection-based libs (Room, Gson) work correctly
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ── Android / Jetpack ────────────────────────────────────────────────────────
# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep @Keep-annotated classes
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# ── Room ─────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static <methods>;
}
-dontwarn androidx.room.**

# ── Gson ─────────────────────────────────────────────────────────────────────
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory { *; }
-keep class * implements com.google.gson.JsonSerializer { *; }
-keep class * implements com.google.gson.JsonDeserializer { *; }
# Keep data model classes used with Gson
-keep class com.bitcraftapps.stax.data.** { *; }
-keepclassmembers class com.bitcraftapps.stax.data.** { *; }

# ── Ktor ─────────────────────────────────────────────────────────────────────
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# ── MediaPipe ────────────────────────────────────────────────────────────────
-keep class com.google.mediapipe.** { *; }
-dontwarn com.google.mediapipe.**

# ── Coil ─────────────────────────────────────────────────────────────────────
-keep class coil.** { *; }
-dontwarn coil.**

# ── Google Play Billing ───────────────────────────────────────────────────────
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# ── Accompanist ──────────────────────────────────────────────────────────────
-keep class com.google.accompanist.** { *; }
-dontwarn com.google.accompanist.**

# ── Jetpack Security (EncryptedSharedPreferences) ───────────────────────────
-keep class androidx.security.** { *; }
-dontwarn androidx.security.**
