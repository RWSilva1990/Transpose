plugins {
    alias(libs.plugins.android.library.compose.convention)
    alias(libs.plugins.android.presentation.ui.convention)
    alias(libs.plugins.android.hilt)
}

android {
    namespace = "com.example.home"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(project(":core:util"))

}