plugins {
    alias(libs.plugins.android.library.compose.convention)
    alias(libs.plugins.android.presentation.ui.convention)

}
android{
    namespace = "com.example.transpose.core.utils"
}

dependencies {
    implementation (libs.prettytime)

}
