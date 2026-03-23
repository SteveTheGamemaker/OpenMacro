# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK tools proguardFiles setting.

# Keep serialization classes
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.openmacro.**$$serializer { *; }
-keepclassmembers class com.openmacro.** {
    *** Companion;
}
-keepclasseswithmembers class com.openmacro.** {
    kotlinx.serialization.KSerializer serializer(...);
}
