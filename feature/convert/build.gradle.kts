plugins {
    alias(libs.plugins.android.library.compose.convention)
    alias(libs.plugins.android.presentation.ui.convention)
    alias(libs.plugins.android.hilt)
}

android {
    namespace = "com.example.convert"
}

dependencies {
    implementation(project(":app"))
    implementation(project(":core:ui"))
}