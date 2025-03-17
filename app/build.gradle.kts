plugins {
    alias(libs.plugins.android.application.compose.convention)
    alias(libs.plugins.android.hilt)
    alias(libs.plugins.android.firebase)
}

android {
    namespace = "com.example.transpose"

}

dependencies {

    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(project(":media"))
    implementation(project(":core:ui"))
    implementation(project(":core:utils"))
    implementation(project(":feature:main"))

}

