plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "transpose.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = "transpose.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "transpose.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }

        register("androidLibraryCompose") {
            id = "transpose.android.library.compose"
             implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidPresentationUI") {
             id = "transpose.android.presentation.ui"
             implementationClass = "AndroidPresentationUIConventionPlugin"
            }
        register("androidHilt"){
            id = "transpose.android.hilt"
            implementationClass = "HiltConventionPlugin"
        }

        register("androidRoom") {
            id = "transpose.android.room"
            implementationClass = "RoomConventionPlugin"
        }
        register("androidNetwork") {
            id = "transpose.android.network"
            implementationClass = "NetworkConventionPlugin"
        }

        register("androidMedia") {
            id = "transpose.android.media"
            implementationClass = "MediaConventionPlugin"
        }

        register("androidViewModel") {
            id = "transpose.android.viewmodel"
            implementationClass = "ViewModelConventionPlugin"
        }
    }
}

dependencies {
    implementation(libs.play.services.dtdi)
    compileOnly(libs.gradle)
    compileOnly(libs.common)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.symbol.processing.gradle.plugin)
    compileOnly(libs.androidx.room.gradle.plugin)
}
