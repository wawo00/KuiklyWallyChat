

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.wally.demo.kuiklywallychat"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.wally.demo.kuiklywallychat"
        minSdk = 28
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":shared"))

    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation(project(":shared"))


    // android原生图片选择
    implementation("io.github.leavesczy:matisse:2.2.0")

    // Matisse 2.2.0 的 CoilImageEngine 编译时使用的版本
//    implementation(libs.coil.compose)
//    implementation(libs.coil.gif)
//    implementation(libs.coil.network.okhttp )
    implementation("io.coil-kt.coil3:coil-compose:3.2.0")
    implementation("io.coil-kt.coil3:coil-gif:3.2.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.2.0")
    implementation("com.tencent.imsdk:imsdk-plus:9.0.7652")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.dynamicanimation:dynamicanimation:1.0.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
}