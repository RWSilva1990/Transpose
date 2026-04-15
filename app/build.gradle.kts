plugins {
    alias(libs.plugins.android.application.compose.convention)
    alias(libs.plugins.android.application)
    alias(libs.plugins.android.hilt)
    alias(libs.plugins.android.firebase)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.example.transpose"

    defaultConfig {
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("debug")
            // 기타 옵션: minifyEnabled 등 원래 release 옵션들 그대로
        }
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }
    lint {
        baseline = file("lint-baseline.xml")
    }
    //    buildTypes {
//        create("benchmark") {
//            initWith(buildTypes.getByName("release"))
//            signingConfig = signingConfigs.getByName("debug")
//            matchingFallbacks += listOf("release")
//            isDebuggable = false
//        }
//    }

}
composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    metricsDestination = layout.buildDirectory.dir("compose_compiler")
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(project(":media"))
    implementation(project(":audio-effect"))
    implementation(project(":core:ui"))
    implementation(project(":core:utils"))
    implementation(project(":feature:main"))
    implementation(libs.androidx.profileinstaller)
    "baselineProfile"(project(":baselineprofile"))
    implementation("androidx.compose.runtime:runtime-tracing:1.0.0-beta01")
    implementation(libs.coil)
    implementation(libs.coil.compose)

}

