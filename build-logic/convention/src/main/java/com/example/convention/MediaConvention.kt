package com.example.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureMedia(
    commonExtension: CommonExtension<*, *, *, *, *>
) {
    dependencies {
        add("implementation", libs.findLibrary("androidx.media3.session").get())
        add("implementation", libs.findLibrary("androidx.media3.exoplayer").get())
        add("implementation", libs.findLibrary("androidx.media3.exoplayer.dash").get())
        add("implementation", libs.findLibrary("androidx.media3.exoplayer.hls").get())
        add("implementation", libs.findLibrary("androidx.media3.ui").get())
        add("implementation", libs.findLibrary("androidx.media3.datasource").get())

    }
}