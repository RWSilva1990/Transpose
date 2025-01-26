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
                    buildTypes{
                        debug {
                            configureDebugBuildType()
                        }
                        create("staging"){
                            configureStagingBuildType()
                        }
                        release {
                            configureReleaseBuildType(commonExtension)
                        }
                    }
                }
            }
            ExtensionType.LIBRARY -> {
                extensions.configure<LibraryExtension>{
                    buildTypes{
                        debug {
                            configureDebugBuildType()
                        }
                        create("staging"){
                            configureStagingBuildType()
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


    isMinifyEnabled = true
    proguardFiles(
    commonExtension.getDefaultProguardFile("proguard-android-optimize.txt"),
    "proguard-rules.pro"
    )
}
