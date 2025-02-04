plugins {
    alias(libs.plugins.android.library.compose.convention)
    alias(libs.plugins.android.presentation.ui.convention)
    alias(libs.plugins.android.hilt)
}

android {
    namespace = "com.example.transpose"

}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":media"))
    implementation(project(":core:ui"))
    implementation(project(":core:utils"))
    implementation(project(":feature:home"))
    implementation(project(":feature:library"))
    implementation(project(":feature:convert"))
}

