plugins {
    alias(libs.plugins.android.library.compose.convention)
    alias(libs.plugins.android.room)
    alias(libs.plugins.android.network)
    alias(libs.plugins.android.hilt)
}

dependencies{
    implementation(project(":core:domain"))
}
