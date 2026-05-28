import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.metro)
    alias(libs.plugins.kotlinter)
    // Apply Google Services plugin to process google-services.json
    alias(libs.plugins.google.services)
    // Apply Firebase Crashlytics plugin for crash reporting
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "dev.hossain.mathtutor"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.hossain.mathtutor"
        minSdk = 28
        targetSdk = 36
        versionCode = 30
        versionName = "1.25.0"

        // Read key or other properties from local.properties
        val localProperties =
            project.rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use {
                Properties().apply { load(it) }
            }
        val apiKey = localProperties?.getProperty("SERVICE_API_KEY") ?: "MISSING-KEY"
        buildConfigField("String", "SERVICE_API_KEY", "\"$apiKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("../keystore/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        create("release") {
            // For CI/CD: Use keystore from environment variables (GitHub Actions)
            // For local builds: Fall back to debug keystore
            val keystoreFile = System.getenv("KEYSTORE_FILE")?.let { rootProject.file(it) }
                ?: file("../keystore/debug.keystore")
            val keystorePassword = System.getenv("KEYSTORE_PASSWORD") ?: "android"
            val keyAliasValue = System.getenv("KEY_ALIAS") ?: "androiddebugkey"
            // Note: Using the same password for both store and key is a common practice and required
            // by the setup documented in RELEASE.md. If you need different passwords, add a KEY_PASSWORD
            // environment variable: System.getenv("KEY_PASSWORD") ?: keystorePassword
            val keyPasswordValue = keystorePassword

            storeFile = keystoreFile
            storePassword = keystorePassword
            keyAlias = keyAliasValue
            keyPassword = keyPasswordValue
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        // Disable Instantiatable lint rule because we use a custom AppComponentFactory
        // (ComposeAppComponentFactory) for dependency injection. Activities are injected
        // via constructor parameters and instantiated by our DI framework (Metro) rather
        // than the Android system's default no-arg constructor mechanism.
        disable += "Instantiatable"
    }
}

kotlin {
    // See https://kotlinlang.org/docs/gradle-compiler-options.html
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.ui.tooling.preview)

    // Firebase BoM (Bill of Materials) - manages all Firebase library versions
    implementation(platform(libs.firebase.bom))
    // Firebase libraries (versions managed by BoM)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    implementation(libs.circuit.codegen.annotations)
    implementation(libs.circuit.foundation)
    implementation(libs.circuit.overlay)
    implementation(libs.circuit.retained)
    implementation(libs.circuitx.android)
    implementation(libs.circuitx.effects)
    implementation(libs.circuitx.gestureNav)
    implementation(libs.circuitx.overlays)
    ksp(libs.circuit.codegen)

    implementation(libs.javax.inject)

    implementation(libs.androidx.work)
    implementation(libs.androidx.datastore.preferences)

    // Timber logging library
    implementation(libs.timber)

    // JSON Syntax Highlighting
    implementation(libs.compose.highlight)

    // Kotlinx Serialization JSON for JSON parsing
    implementation(libs.kotlinx.serialization.json)

    // AndroidX Browser (Custom Tabs) for opening external links
    implementation(libs.androidx.browser)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Media3 ExoPlayer for audio playback
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.common)

    // Material 3 Adaptive layouts for tablet support
    // https://developer.android.com/develop/ui/compose/layouts/adaptive
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.compose.material3.adaptive.navigationSuite)
    implementation(libs.androidx.compose.materialWindow)
    implementation(libs.androidx.window)

    // LeakCanary - Memory leak detection (debug builds only)
    // https://square.github.io/leakcanary/
    debugImplementation(libs.leakcanary.android)

    // Testing
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.truth)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.androidx.ui.tooling)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.truth)
}

ksp {
    // Circuit-KSP configuration for Metro DI integration
    // See https://slackhq.github.io/circuit/code-gen/
    arg("circuit.codegen.mode", "metro")
    
    // Room schema export location for database migrations
    // See https://developer.android.com/training/data-storage/room/migrating-db-versions
    arg("room.schemaLocation", "$projectDir/schemas")
}

metro {
    // Enable Metro debug mode for better logging and debugging support
    // When enabled, Metro will emit detailed debug information about the dependency graph
    // See https://zacsweers.github.io/metro/latest/
    debug.set(true)
}
