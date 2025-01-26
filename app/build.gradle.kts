plugins {
    alias(libs.plugins.android.application.compose.convention)
    alias(libs.plugins.android.hilt)
}

android {
    namespace = "com.example.transpose"

}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":media"))
    implementation(project(":core:ui"))
    implementation(project(":feature:home"))
    implementation(project(":feature:library"))
    implementation(project(":feature:convert"))


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.material3.v121)

    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.litert.metadata)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material)


    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    // ViewModel utilities for Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // LiveData
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Lifecycle utilities for Compose
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Saved state module for ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)

    implementation (libs.newpipeextractor)
    implementation (libs.jsoup)
    implementation(libs.okhttp)

    implementation(libs.retrofit)
    implementation (libs.converter.gson)

    implementation (libs.prettytime)
    implementation(libs.coil.compose)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.ui)

    implementation (libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.hilt.navigation.compose)


    // optional - Kotlin Extensions and Coroutines support for Room
    implementation(libs.compose.shimmer)
    implementation (libs.accompanist.systemuicontroller)

}

