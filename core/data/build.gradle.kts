plugins {
    alias(libs.plugins.android.library.compose.convention)
    alias(libs.plugins.android.room)
    alias(libs.plugins.android.network)
    alias(libs.plugins.android.hilt)
}
android{
    namespace = "com.example.transpose.core.data"
}

dependencies{
    implementation(project(":core:domain"))
}
