plugins {
    alias(libs.plugins.android.application.compose.convention)
    alias(libs.plugins.android.hilt)
    alias(libs.plugins.android.firebase)
    alias(libs.plugins.android.application)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.example.transpose"
    buildTypes {
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

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(project(":media"))
    implementation(project(":core:ui"))
    implementation(project(":core:utils"))
    implementation(project(":feature:main"))
    implementation(libs.androidx.profileinstaller)
    "baselineProfile"(project(":baselineprofile"))
    implementation("androidx.compose.runtime:runtime-tracing:1.0.0-beta01")

}

