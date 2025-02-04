plugins {
    alias(libs.plugins.android.library.compose.convention)
    alias(libs.plugins.android.room)
    alias(libs.plugins.android.network)
}

android{
    namespace = "com.example.transpose.core.domain"
}

