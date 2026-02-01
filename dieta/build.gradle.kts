plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)                // para Room (KSP)
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.dieta"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        // sem compose aqui
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    // Core Kotlin / AndroidX base
    implementation(libs.androidx.core.ktx)

    // SQLite KTX (opcional, mas ok usar)
    // Garante que a versão em libs.versions.toml é 2.6.2, que é a recomendada com Room 2.8.4
    implementation(libs.androidx.sqlite.ktx) // ou implementation("androidx.sqlite:sqlite-ktx:2.6.2") [web:3][web:6]

    // Room (apenas os artefactos Android)
    val roomVersion = "2.8.4"
    implementation("androidx.room:room-runtime:$roomVersion")   // núcleo Room [web:2]
    implementation("androidx.room:room-ktx:$roomVersion")       // coroutines/ktx [web:3]
    ksp("androidx.room:room-compiler:$roomVersion")

    // NÃO usar libs.androidx.room.runtime.jvm aqui (remove essa linha)

    // Retrofit + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.52")
    kapt("com.google.dagger:hilt-compiler:2.52")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
