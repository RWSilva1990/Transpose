package com.example.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType

internal fun Project.configureHilt() {
    with(pluginManager) {
        apply("com.google.dagger.hilt.android")
        apply("kotlin-kapt")
    }

    dependencies {
        "implementation"(libs.findLibrary("hilt.android").get())
        "kapt"(libs.findLibrary("hilt.compiler").get())
    }

    // kapt 설정
    extensions.findByType<org.jetbrains.kotlin.gradle.plugin.KaptExtension>()?.apply {
        correctErrorTypes = true
    }
}