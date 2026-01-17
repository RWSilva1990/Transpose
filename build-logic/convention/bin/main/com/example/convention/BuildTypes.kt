package com.example.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.gradle.api.Project
import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.LibraryExtension
import org.gradle.kotlin.dsl.configure

internal fun Project.configureBuildTypes(
    commonExtension: CommonExtension<*, *, *, *, *>,
    extensionType: ExtensionType
){
    commonExtension.run {
        buildFeatures{
            buildConfig = true
        }

        when(extensionType){
            ExtensionType.APPLICATION -> {
                extensions.configure<ApplicationExtension>{

                    signingConfigs {
                        create("benchmarkDebug") {
                            storeFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
                            storePassword = "android"
                            keyAlias = "androiddebugkey"
                            keyPassword = "android"
                        }
                    }
                    buildTypes{
                        debug {
                            configureDebugBuildType()
                        }
                        release {
                            configureReleaseBuildType(commonExtension)
                        }
//                        create ("benchmark") {
//                            isDebuggable = false
//                            configureBenchmarkBuildType(commonExtension)
//                            signingConfig = signingConfigs.getByName("benchmarkDebug")
//                        }
                    }
                }
            }
            ExtensionType.LIBRARY -> {
                extensions.configure<LibraryExtension>{
                    buildTypes{
                        debug {
                            configureDebugBuildType()
                        }
                        release {
                            configureReleaseBuildType(commonExtension)
                        }
                    }
                }
            }
        }

    }
}

private fun BuildType.configureDebugBuildType() {
    buildConfigField("String", "BASE_URL", "\"DEBUG_API_URL\"")
}

private fun BuildType.configureStagingBuildType() {
    buildConfigField("String", "BASE_URL", "\"STAGING_API_URL\"")
}

private fun BuildType.configureReleaseBuildType(
    commonExtension: CommonExtension<*, *, *, *, *>

) {
    buildConfigField("String", "BASE_URL", "\"RELEASE_API_URL\"")

    isMinifyEnabled = false
    proguardFiles(
    commonExtension.getDefaultProguardFile("proguard-android-optimize.txt"),
    "proguard-rules.pro"
    )
}

private fun BuildType.configureBenchmarkBuildType(
    commonExtension: CommonExtension<*, *, *, *, *>
) {

    isMinifyEnabled = false
    matchingFallbacks += listOf("release")

    proguardFiles(
        commonExtension.getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}

