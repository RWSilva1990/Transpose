package com.example.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureNetwork(
    commonExtension: CommonExtension<*, *, *, *, *>
) {
    dependencies {
        add("implementation", libs.findLibrary("retrofit").get())
        add("implementation", libs.findLibrary("converter.gson").get())
        add("implementation", libs.findLibrary("okhttp").get())
        add("implementation", libs.findLibrary("newpipeextractor").get())
        add("implementation", libs.findLibrary("jsoup").get())
        add("implementation", libs.findLibrary("prettytime").get())
    }
}