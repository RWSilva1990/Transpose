plugins {
    alias(libs.plugins.android.library.compose.convention)
    alias(libs.plugins.android.hilt)
    alias(libs.plugins.android.media)
}

android {
    namespace = "com.example.media"

}

dependencies{
    implementation(project(":core:domain"))
    implementation(project(":core:utils"))
    implementation(project(":audio"))
}