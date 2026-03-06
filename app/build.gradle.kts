import org.gradle.kotlin.dsl.implementation
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.text.SimpleDateFormat

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.baseproject"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.baseproject"
        minSdk = 29
        targetSdk = 36
//        versionCode = 100
//        versionName = "1.0.0"

        versionCode = 1
        versionName = "test"

        val dateTime = SimpleDateFormat("dd-MM-yyyy").format(System.currentTimeMillis())
        setProperty("archivesBaseName", "Base - Project ($versionCode)_$dateTime")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

//    kotlin.compilerOptions{
//        jvmTarget.set(JvmTarget.JVM_17)
//    }

    buildFeatures {
        viewBinding = true
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
    ndkVersion = "29.0.14206865"
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //ads library
    implementation(libs.ssquadadslibrary)

    //other library
    implementation(libs.lottie)
    implementation(libs.dotsindicator)
    implementation(libs.glide)
    implementation(libs.user.messaging.platform)

    implementation(libs.gson)
    implementation(project(":openCV"))  // OpenCV dependency
    implementation(project(":drawingview"))

    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

}