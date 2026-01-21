import io.grpc.InternalChannelz.id
import org.gradle.kotlin.dsl.implementation

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

// Configuration for Kotlin Annotation Processing Tool (Kapt)
kapt {
    // Ensures that Kapt uses the correct type for error messages, improving compile-time checks.
    correctErrorTypes = true
}

android {
    namespace = "com.day.mate"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.day.mate"
        minSdk = 29
        targetSdk = 36
        versionCode = 4
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Global lint configuration block.
    lint {
        disable += "NullSafeMutableLiveData" // Disables a specific lint check related to NullSafeMutableLiveData usage.
    }

    signingConfigs {
        create("shared") {
            // IMPORTANT: Ensure the 'daymate.keystore' file is located in the 'app/' directory.
            storeFile = file("daymate.keystore")
            storePassword = "123456"
            keyAlias = "daymate_key"
            keyPassword = "123456"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Set to true for production minification
            signingConfig = signingConfigs.getByName("shared") // Applies the shared signing configuration for secure release builds.
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
           signingConfig = signingConfigs.getByName("shared") // Uses the same shared signing configuration for debug builds.
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        // Enables Jetpack Compose support
        compose = true
    }
}


dependencies {


    // ==========================================================
    // Core AndroidX and Lifecycle Libraries
    // ==========================================================
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation(libs.androidx.appcompat)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.compose.material3:material3:1.3.0")

    // ==========================================================
    // Jetpack Compose Dependencies
    // ==========================================================
    // Compose Bill of Materials (BOM) to manage consistent versioning
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")
    implementation("androidx.navigation:navigation-compose:2.8.5") // Compose Navigation
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")


    // ==========================================================
    // Persistence (Room Database)
    // ==========================================================
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1") // Kotlin extensions for Room (Coroutines support)
    kapt("androidx.room:room-compiler:2.6.1") // Kapt processor for Room


    // ==========================================================
    // Coroutines
    // ==========================================================
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1") // Essential for asynchronous operations


    // ==========================================================
    // Firebase Platform (BOM, Core, Auth, Firestore)
    // ==========================================================
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore") // Cloud Firestore database

    implementation ("com.google.android.gms:play-services-auth")

    // ==========================================================
    // Networking
    // ==========================================================
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // GSON converter for JSON serialization
    implementation("com.google.code.gson:gson:2.10.1")


    // ==========================================================
    // Image Loading and UI Utilities
    // ==========================================================
    implementation("io.coil-kt:coil-compose:2.7.0") // Coil for efficient image loading in Compose
    implementation("io.github.vanpra.compose-material-dialogs:datetime:0.9.0") // DatePicker dialogs for Compose
    implementation("com.google.accompanist:accompanist-pager:0.32.0") // Accompanist Pager library
    implementation("com.google.accompanist:accompanist-pager-indicators:0.32.0") // Pager indicators


    // ==========================================================
    // Media and Background Tasks
    // ==========================================================
    implementation("androidx.media3:media3-exoplayer:1.4.1") // Modern media playback solution
    implementation("androidx.media3:media3-ui:1.4.1")
    implementation("androidx.work:work-runtime-ktx:2.9.0") // WorkManager for robust background tasks


    // ==========================================================
    // Authentication & Security
    // ==========================================================
    implementation("com.google.firebase:firebase-auth-ktx:23.2.1")
    implementation("com.google.android.gms:play-services-auth:21.4.0") // Google Sign-In SDK
    implementation("androidx.biometric:biometric:1.1.0") // Biometric authentication support
    implementation("androidx.appcompat:appcompat:1.6.1")


    // ==========================================================
    // 🧪 TESTING DEPENDENCIES (Unit Tests - src/test)
    // ==========================================================

    testImplementation("junit:junit:4.13.2") // Standard JUnit 4

    // AndroidX Test Core: Provides InstantTaskExecutorRule for testing LiveData/ViewModel in JVM
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // Kotlin Coroutines Testing: Crucial for testing Coroutine-based ViewModels and Flows
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

    // MockK: Modern mocking library for Kotlin
    testImplementation("io.mockk:mockk:1.13.11")

    // Mockito: Core mocking framework with Kotlin extensions and inline support
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-inline:5.2.0") // Enables mocking of final classes/methods
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1") // Kotlin language integration for Mockito

    // Turbine: Highly recommended for testing Kotlin Flows (simplifies assertion on emissions)
    testImplementation("app.cash.turbine:turbine:1.1.0")


    // ==========================================================
    // UI Testing (Instrumented Tests - src/androidTest)
    // ==========================================================

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    // Compose Testing (required for UI interaction tests)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.8")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.8")
}