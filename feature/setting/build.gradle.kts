plugins {
    alias(libs.plugins.android.library.compose.convention)
    alias(libs.plugins.android.presentation.ui.convention)
    alias(libs.plugins.android.hilt)
}

android {
    namespace = "com.example.setting"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(project(":core:utils"))
    implementation(project(":media"))

}