package com.example.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureViewModel(
    commonExtension: CommonExtension<*, *, *, *, *>
) {
    dependencies {
        add("implementation", libs.findLibrary("androidx.lifecycle.runtime.ktx").get())
        add("implementation", libs.findLibrary("androidx.lifecycle.viewmodel.ktx").get())
        add("implementation", libs.findLibrary("androidx.lifecycle.viewmodel.compose").get())
        add("implementation", libs.findLibrary("androidx.lifecycle.livedata.ktx").get())
        add("implementation", libs.findLibrary("androidx.lifecycle.runtime.compose").get())
        add("implementation", libs.findLibrary("androidx.lifecycle.viewmodel.savedstate").get())
    }
}