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

# ============================================================================
# Circuit UDF Framework - Keep all Circuit classes and related code
# ============================================================================
-keep interface com.slack.circuit.** { *; }
-keepclassmembers class * implements com.slack.circuit.runtime.CircuitUiState { *; }
-keepclassmembers class * implements com.slack.circuit.runtime.CircuitUiEvent { *; }

# Circuit codegen generated classes
-keep class dev.hossain.mathtutor.** implements com.slack.circuit.runtime.presenter.Presenter { *; }
-keep class dev.hossain.mathtutor.** implements com.slack.circuit.runtime.screen.Screen { *; }

# Circuit may reference Dagger Hilt annotations (even with Metro) - ignore missing references
-dontwarn dagger.hilt.GeneratesRootInput

# ============================================================================
# Metro Dependency Injection - Keep all Metro bindings and injection code
# ============================================================================
-keep class com.zacsweers.metro.** { *; }
-keep interface com.zacsweers.metro.** { *; }
-keepclassmembers class * { 
    @javax.inject.Inject <init>(...); 
    @javax.inject.Inject <fields>;
}

# ============================================================================
# Kotlin Serialization - Required for JSON serialization/deserialization
# ============================================================================
-keepclassmembers class dev.hossain.mathtutor.domain.model.** {
    *** serialize(...);
}
-keep class dev.hossain.mathtutor.domain.model.**$serializer { *; }
-keep class dev.hossain.mathtutor.domain.model.**$$serializer { *; }
-keepclassmembers class * implements kotlinx.serialization.KSerializer {
    *** objectSerializer(...);
}

# ============================================================================
# Reflection-based code - Keep classes that use reflection
# ============================================================================
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclasseswithmembernames class * {
    *** *(...);
}

# ============================================================================
# Enum classes - Keep all enum constructors and methods
# ============================================================================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ============================================================================
# Data classes and model classes - Keep constructors and properties
# ============================================================================
-keep class dev.hossain.mathtutor.domain.model.** { *; }

# ============================================================================
# Remove logging in release builds (optional but recommended)
# ============================================================================
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Timber logging library
-keep class timber.log.Timber { *; }
-keepclassmembers class timber.log.Timber** { *; }