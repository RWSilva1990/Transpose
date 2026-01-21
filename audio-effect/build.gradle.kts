plugins {
    alias(libs.plugins.android.library.compose.convention)
    alias(libs.plugins.android.hilt)
}

android {
    namespace = "com.example.audio_effect"

    defaultConfig {
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }

        externalNativeBuild {
            cmake {
                arguments += listOf(
                    "-DANDROID_PLATFORM=android-26",
                    "-DANDROID_TOOLCHAIN=clang",
                    "-DANDROID_ARM_NEON=TRUE",
                    "-DANDROID_STL=c++_static"
                )
                cFlags += listOf("-O3", "-fsigned-char")
                cppFlags += listOf("-fsigned-char")
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    ndkVersion = "27.0.12077973"
}

dependencies {
    implementation(project(":core:utils"))
    implementation(libs.okhttp)
}
