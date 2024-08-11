plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.devtools.ksp")
    alias(libs.plugins.compose.compiler)
    id("dagger.hilt.android.plugin")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    kotlin("plugin.serialization") version "2.0.0"
    alias(libs.plugins.google.gms.google.services)
    id("androidx.navigation.safeargs")
    id("androidx.room")
}

android {
    namespace = "com.example.craite"
    compileSdk = 34

    packagingOptions {
        jniLibs.pickFirsts.add("lib/*/libavdevice_neon.so")
        pickFirst("lib/x86/libc++_shared.so")
        pickFirst("lib/x86_64/libc++_shared.so")
        pickFirst("lib/armeabi-v7a/libc++_shared.so")
        pickFirst("lib/arm64-v8a/libc++_shared.so")

        pickFirst("lib/arm64-v8a/libavcodec.so")
        pickFirst("lib/arm64-v8a/libavformat.so")
        pickFirst("lib/arm64-v8a/libavutil.so")
        pickFirst("lib/arm64-v8a/libswscale.so")
        pickFirst("lib/arm64-v8a/libavdevice.so")
        pickFirst("lib/arm64-v8a/libavfilter.so")
        pickFirst("lib/arm64-v8a/libffmpegkit.so")
        pickFirst("lib/arm64-v8a/libffmpegkit_abidetect.so")
        pickFirst("lib/arm64-v8a/libswresample.so")
        pickFirst("lib/arm64-v8a/libswscale.so")
        pickFirst("lib/x86/libavcodec.so")
        pickFirst("lib/x86/libavformat.so")
        pickFirst("lib/x86/libavutil.so")
        pickFirst("lib/x86/libswscale.so")
        pickFirst("lib/x86/libavdevice.so")
        pickFirst("lib/x86/libavfilter.so")
        pickFirst("lib/x86/libffmpegkit.so")
        pickFirst("lib/x86/libffmpegkit_abidetect.so")
        pickFirst("lib/x86/libswresample.so")
        pickFirst("lib/x86/libswscale.so")
        pickFirst("lib/x86_64/libavcodec.so")
        pickFirst("lib/x86_64/libavformat.so")
        pickFirst("lib/x86_64/libavutil.so")
        pickFirst("lib/x86_64/libswscale.so")
        pickFirst("lib/x86_64/libavdevice.so")
        pickFirst("lib/x86_64/libavfilter.so")
        pickFirst("lib/x86_64/libffmpegkit.so")
        pickFirst("lib/x86_64/libffmpegkit_abidetect.so")
        pickFirst("lib/x86_64/libswresample.so")
        pickFirst("lib/x86_64/libswscale.so")
        pickFirst("lib/armeabi-v7a/libavcodec_neon.so")
    }

    defaultConfig {
        applicationId = "com.example.craite"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        room {
            schemaDirectory("$projectDir/schemas")
        }
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("debug")
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
        kotlinOptions {
            jvmTarget = "1.8"
        }
        buildFeatures {
            compose = true
            viewBinding = true
            dataBinding = true
            var room = true
        }
        composeOptions {
            kotlinCompilerExtensionVersion = "1.5.14"
        }
        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }

    }

    dependencies {
        implementation(libs.firebase.storage)
        implementation(libs.firebase.functions)
        implementation(libs.firebase.firestore.ktx)
        implementation(libs.androidx.foundation.layout.android)
        //  implementation(libs.androidx.material3.android) Remove Material 3 Android (M3 for Non Compose Android Dev)
        val lifecycleVersion = "2.8.1"
        val activityVersion = "1.9.0"
        val navVersion = "2.7.7"
        val roomVersion = "2.6.1"

        // ViewModel
        implementation(libs.androidx.lifecycle.viewmodel.ktx)
        // ViewModel utilities for Compose
        implementation(libs.androidx.lifecycle.viewmodel.compose)
        // LiveData
        implementation(libs.androidx.lifecycle.livedata.ktx)
        // Lifecycles only (without ViewModel or LiveData)
        implementation(libs.androidx.lifecycle.runtime.ktx.v281)
        // Lifecycle utilities for Compose
        implementation(libs.androidx.lifecycle.runtime.compose)

        // Saved state module for ViewModel
        implementation(libs.androidx.lifecycle.viewmodel.savedstate)

        implementation(libs.androidx.media3.transformer)
        implementation(libs.androidx.media3.effect)
        implementation(libs.androidx.media3.common)

        implementation(libs.androidx.media3.exoplayer)
        implementation(libs.androidx.media3.exoplayer.dash)
        implementation(libs.androidx.media3.ui)

        implementation(libs.androidx.hilt.navigation.compose)

        implementation(libs.androidx.activity.ktx)

        implementation(libs.androidx.navigation.fragment.ktx)
        implementation(libs.androidx.navigation.ui.ktx)
        implementation(libs.androidx.navigation.compose)

        implementation(libs.androidx.room.runtime)
        annotationProcessor(libs.room.compiler)
        implementation(libs.room.ktx)
        ksp(libs.room.compiler)

        implementation("com.github.bumptech.glide:glide:4.16.0")
        annotationProcessor(libs.compiler)

        implementation("com.arthenica:ffmpeg-kit-full:6.0-2")

        implementation("com.google.code.gson:gson:2.11.0")
        implementation("com.squareup.retrofit2:retrofit:2.11.0")
        implementation("com.squareup.retrofit2:converter-gson:2.11.0")
        implementation("com.squareup.okhttp3:okhttp:4.9.1")
        implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")

        implementation("com.android.volley:volley:1.2.1")

        implementation("androidx.compose.foundation:foundation:1.6.7")

        implementation("androidx.compose.ui:ui:1.6.7")

        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

        implementation("com.google.dagger:hilt-android:2.51.1")
        ksp("com.google.dagger:hilt-compiler:2.51.1")

        implementation("com.google.android.gms:play-services-location:21.3.0")

        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

        implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
        implementation("com.google.firebase:firebase-auth")

        // kotlin serialization
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

        // coil for images
        implementation("io.coil-kt:coil:2.6.0")
        implementation("io.coil-kt:coil-compose:2.6.0")

        // kotlin coroutines
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

        //Material Icons Extended
        implementation("androidx.compose.material:material-icons-extended")

        //Fluent UI
        // implementation("com.microsoft.fluentui:FluentUIAndroid:0.2.9")
        implementation("com.microsoft.fluentui:fluentui_icons:0.2.0")

        //Google Fonts
        implementation("androidx.compose.ui:ui-text-google-fonts:1.6.8")

        // FFmpeg kit

        // Transcoder
        implementation("com.otaliastudios:transcoder:0.10.5")

        implementation("com.writingminds:FFmpegAndroid:0.3.2")

        //Splash Screen
        implementation("androidx.core:core-splashscreen:1.0.1")

        //ExoPlayer
        implementation("com.google.android.exoplayer:exoplayer:2.18.7")




        implementation(libs.material3)
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.ui)
        implementation(libs.androidx.ui.graphics)
        implementation(libs.androidx.ui.tooling.preview)
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.ui.test.junit4)
        debugImplementation(libs.androidx.ui.tooling)
        debugImplementation(libs.androidx.ui.test.manifest)
    }
}